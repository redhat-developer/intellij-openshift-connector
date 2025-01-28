/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.oauth;

import org.jboss.tools.intellij.openshift.oauth.exception.OAuthConfigurationException;
import org.jboss.tools.intellij.openshift.oauth.exception.OAuthLoginException;
import org.jboss.tools.intellij.openshift.oauth.exception.OAuthRefreshException;
import org.jboss.tools.intellij.openshift.oauth.model.AccountModel;
import org.jboss.tools.intellij.openshift.oauth.model.IAccount;
import org.jboss.tools.intellij.openshift.oauth.model.IAccountModel;
import org.jboss.tools.intellij.openshift.oauth.model.IAuthorizationServer;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.representations.AccessTokenResponse;

import java.util.List;
import java.util.Optional;

public class AccountService {

  private static final AccountService INSTANCE = new AccountService();

  private IAccountModel model;

  private AccountService() {
  }

  public static AccountService getDefault() {
    return INSTANCE;
  }

  private IAccountModel getModel() {
    if (null == model) {
      model = new AccountModel();
    }
    return model;
  }

  public AccountStatus getStatus(IAccount account) {
    if (account.getAccessToken() == null) {
      return AccountStatus.NEEDS_LOGIN;
    }
    long lastRefreshed = account.getLastRefreshedTime();
    long current = System.currentTimeMillis();
    if (current > account.getAccessTokenExpiryTime()) {
      if (current > account.getRefreshTokenExpiryTime()) {
        return AccountStatus.NEEDS_LOGIN;
      } else {
        return AccountStatus.NEEDS_REFRESH;
      }
    }
    if (wasRefreshed24HAgo(lastRefreshed, current) || wasRefreshedMoreThanHalfTheTotalValidPeriod(
      account.getAccessTokenExpiryTime(), lastRefreshed, current)) {
      return AccountStatus.NEEDS_REFRESH;
    }
    return AccountStatus.VALID;
  }

  boolean wasRefreshedMoreThanHalfTheTotalValidPeriod(long expiryTime, long lastRefreshed, long current) {
    return (current - lastRefreshed) > (expiryTime - current);
  }

  boolean wasRefreshed24HAgo(long lastRefreshed, long current) {
    return (current - lastRefreshed) > OAuthCoreConstants.DURATION_24_HOURS;
  }

  private IAuthorizationServer findAuthorizationServer(String serverId) {
    Optional<IAuthorizationServer> server = getModel().getAuthorizationServers().stream().filter(cl -> serverId.equals(cl.getId())).findFirst();
    return server.orElse(null);
  }

  public String getToken(String serverId, int tokenType, Object context) {
    String token = null;

    IAuthorizationServer server = findAuthorizationServer(serverId);
    if (server != null) {
      List<IAccount> identities = server.getAccounts();
      if (identities.isEmpty()) {
        token = performLogin(server, null, tokenType, context);
      } else {
        IAccount account = identities.get(0);
        AccountStatus status = getStatus(account);
        switch (status) {
          case VALID:
            token = account.getToken(tokenType);
            break;
          case NEEDS_REFRESH:
            token = performRefresh(account, tokenType);
            break;
          case NEEDS_LOGIN:
            token = performLogin(server, account, tokenType, context);
            break;
        }

      }
      return token;
    } else {
      throw new OAuthConfigurationException("No server found for id: " + serverId);
    }
  }


  private String performLogin(IAuthorizationServer server, IAccount account, int tokenType, Object context) {
    LoginProvider provider = LoginProvider.get();
    if (null != provider) {
      LoginResponse response = provider.login(server, context);
      if (null != response) {
        if (null == account) {
          IAccount newAccount = createAccount(server, response);
          return newAccount.getToken(tokenType);
        } else {
          updateAccount(response, account);
        }
        return account.getToken(tokenType);
      } else {
        throw new OAuthLoginException(server, account);
      }
    } else {
      throw new OAuthConfigurationException("No login provider found");
    }
  }

  IAccount createAccount(IAuthorizationServer server, LoginResponse response) {
    String id = OAuthUtils.decodeEmailFromToken(server, response.getIDToken());
    IAccount newAccount = server.createAccount(id);
    updateAccount(response, newAccount);
    server.addAccount(newAccount);
    return newAccount;
  }

  void updateAccount(LoginResponse info, IAccount account) {
    account.setIDToken(info.getIDToken());
    account.setAccessToken(info.getAccessToken());
    account.setRefreshToken(info.getRefreshToken());
    account.setLastRefreshedTime(System.currentTimeMillis());
    account.setAccessTokenExpiryTime(info.getAccessTokenExpiryTime());
    account.setRefreshTokenExpiryTime(info.getRefreshTokenExpiryTime());
  }

  private String performRefresh(IAccount account, int tokenType) {
    try {
      KeycloakDeployment deployment = OAuthUtils.getDeployment(account.getAuthorizationServer());
      AccessTokenResponse response = ServerRequest.invokeRefresh(deployment, account.getRefreshToken());
      account.setIDToken(response.getIdToken());
      account.setAccessToken(response.getToken());
      account.setRefreshToken(response.getRefreshToken());
      account.setAccessTokenExpiryTime(System.currentTimeMillis() + response.getExpiresIn() * 1000);
      account.setRefreshTokenExpiryTime(System.currentTimeMillis() + response.getRefreshExpiresIn() * 1000);
      account.setLastRefreshedTime(System.currentTimeMillis());
      return account.getToken(tokenType);
    } catch (Exception e) {
      throw new OAuthRefreshException(account, e);
    }
  }
}
