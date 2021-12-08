/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.oauth.model;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jboss.tools.intellij.openshift.Constants;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.net.CacheRequest;
import java.util.ArrayList;
import java.util.List;

@State(name = "OAuthAccounts", storages =
        @Storage("oauthaccounts.xml")
, reportStatistic = false)
@Service
public final class ServersRepository implements PersistentStateComponent<Element> {
    private static final String SERVER_TAG = "server";
    private static final String ID_ATTRIBUTE = "id";
    private static final String ACCOUNT_TAG = "account";
    private static final String ID_TOKEN_ATTRIBUTE = "idToken";
    private static final String ACCESS_TOKEN_ATTRIBUTE = "accessToken";
    private static final String REFRESH_TOKEN_ATTRIBUTE = "refreshToken";
    private static final String ACCESS_TOKEN_EXPIRES_IN_ATTRIBUTE = "accessTokenExpiresIn";
    private static final String REFRESH_TOKEN_EXPIRES_IN_ATTRIBUTE = "refreshTokenExpiresIn";
    private static final String LAST_REFRESHED_TIME_ATTRIBUTE = "lastRefreshedTime";
    private static final String SERVERS_TAG = "servers";
    private List<IAuthorizationServer> servers = new ArrayList<>();

    private CredentialAttributes getAttributes(IAccount account, String attribute) {
        return new CredentialAttributes(Constants.PLUGIN_ID + '.' + account.getAuthorizationServer().getId() + '.' + account.getId() +
                "." + attribute);
    }

    private Element account2Node(IAccount account) {
        Element node = new Element(ACCOUNT_TAG);
        node.setAttribute(ID_ATTRIBUTE, account.getId());
        PasswordSafe.getInstance().set(getAttributes(account, ID_TOKEN_ATTRIBUTE), new Credentials(null, account.getIDToken()));
        PasswordSafe.getInstance().set(getAttributes(account, ACCESS_TOKEN_ATTRIBUTE), new Credentials(null, account.getAccessToken()));
        PasswordSafe.getInstance().set(getAttributes(account, REFRESH_TOKEN_ATTRIBUTE), new Credentials(null, account.getRefreshToken()));
        node.setAttribute(ACCESS_TOKEN_EXPIRES_IN_ATTRIBUTE, String.valueOf(account.getAccessTokenExpiryTime()));
        node.setAttribute(REFRESH_TOKEN_EXPIRES_IN_ATTRIBUTE, String.valueOf(account.getRefreshTokenExpiryTime()));
        node.setAttribute(LAST_REFRESHED_TIME_ATTRIBUTE, String.valueOf(account.getLastRefreshedTime()));
        return node;
    }
    private Element server2Node(IAuthorizationServer server) {
        Element node = new Element(SERVER_TAG);
        node.setAttribute(ID_ATTRIBUTE, server.getId());
        for(IAccount account : server.getAccounts()) {
            node.addContent(account2Node(account));
        }
        return node;
    }

    @Override
    public @Nullable Element getState() {
        Element root = new Element(SERVERS_TAG);
        for(IAuthorizationServer server : servers) {
            root.addContent(server2Node(server));
        }
        return root;
    }

    @Override
    public void loadState(@NotNull Element state) {
        servers.clear();
        for(Element serverNode : state.getChildren(SERVER_TAG)) {
            IAuthorizationServer server = new AuthorizationServer(serverNode.getAttributeValue(ID_ATTRIBUTE));
            for(Element accountNode : serverNode.getChildren(ACCOUNT_TAG)) {
                IAccount account = new Account(accountNode.getAttributeValue(ID_ATTRIBUTE), server);
                account.setIDToken(PasswordSafe.getInstance().getPassword(getAttributes(account, ID_TOKEN_ATTRIBUTE)));
                account.setAccessToken(PasswordSafe.getInstance().getPassword(getAttributes(account, ACCESS_TOKEN_ATTRIBUTE)));
                account.setRefreshToken(PasswordSafe.getInstance().getPassword(getAttributes(account, REFRESH_TOKEN_ATTRIBUTE)));
                account.setAccessTokenExpiryTime(Long.parseLong(accountNode.getAttributeValue(ACCESS_TOKEN_EXPIRES_IN_ATTRIBUTE)));
                account.setRefreshTokenExpiryTime(Long.parseLong(accountNode.getAttributeValue(REFRESH_TOKEN_EXPIRES_IN_ATTRIBUTE)));
                account.setLastRefreshedTime(Long.parseLong(accountNode.getAttributeValue(LAST_REFRESHED_TIME_ATTRIBUTE)));
                server.addAccount(account);
            }
            servers.add(server);
        }
    }

    public List<IAuthorizationServer> getServers() {
        return servers;
    }

    public void setServers(List<IAuthorizationServer> servers) {
        this.servers = servers;
    }
}
