/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.oauth.model;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jboss.tools.intellij.openshift.oauth.TokenProvider;

import java.io.IOException;


@Tag("account")
public class Account implements IAccount {

	private static final String ACCESS_TOKEN_EXPIRY_TIME_KEY = "accessTokenExpiryTime";

	private static final String REFRESH_TOKEN_EXPIRY_TIME_KEY = "refreshTokenExpiryTime";

	private static final String LAST_REFRESHED_TIME_KEY = "lastRefreshTime";

	private static final String ID_TOKEN_KEY = "IDToken";
	
	private static final String ACCESS_TOKEN_KEY = "accessToken";

	private static final String REFRESH_TOKEN_KEY = "refreshToken";

	@Attribute
	private String id;

	@Attribute("IDToken")
	private String idToken;

	@Attribute
	private String accessToken;

	@Attribute
	private String refreshToken;

	@Attribute
	private long accessTokenExpiryTime = Long.MAX_VALUE;

	@Attribute
	private long refreshTokenExpiryTime = Long.MAX_VALUE;

	@Attribute
	private long lastRefreshedTime;

	@Attribute
	private IAuthorizationServer server;

	public Account(String id, IAuthorizationServer server) {
		this.id = id;
		this.server = server;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public IAuthorizationServer getAuthorizationServer() {
		return server;
	}

	/**
   * @return the idToken
   */
  public String getIDToken() {
    return idToken;
  }

  /**
   * @param idToken the idToken to set
   */
  public void setIDToken(String idToken) {
    this.idToken = idToken;
  }

  @Override
	public String getAccessToken() {
		return accessToken;
	}

	@Override
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public String getRefreshToken() {
		return refreshToken;
	}

	@Override
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	@Override
  public String getToken(int tokenType) {
	  switch (tokenType) {
	  case TokenProvider.ID_TOKEN:
	    return getIDToken();
	  case TokenProvider.ACCESS_TOKEN:
	    return getAccessToken();
	  case TokenProvider.REFRESH_TOKEN:
	    return getRefreshToken();
	  }
    return null;
  }

  @Override
	public long getAccessTokenExpiryTime() {
		return accessTokenExpiryTime;
	}

	@Override
	public void setAccessTokenExpiryTime(long accessTokenExpiryTime) {
		this.accessTokenExpiryTime = accessTokenExpiryTime;
	}

	@Override
	public long getRefreshTokenExpiryTime() {
		return refreshTokenExpiryTime;
	}

	@Override
	public void setRefreshTokenExpiryTime(long refreshTokenExpiryTime) {
		this.refreshTokenExpiryTime = refreshTokenExpiryTime;
	}

	@Override
	public long getLastRefreshedTime() {
		return lastRefreshedTime;
	}

	@Override
	public void setLastRefreshedTime(long lastRefreshTime) {
		this.lastRefreshedTime = lastRefreshTime;
	}

}
