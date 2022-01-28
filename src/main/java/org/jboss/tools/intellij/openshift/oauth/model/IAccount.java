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

/**
 * Represents an OSIO account stored in database.
 */
public interface IAccount {
	/**
	 * The unique identifier (email) for the account.
	 * 
	 * @return the account id
	 */
	String getId();

	IAuthorizationServer getAuthorizationServer();
	
	String getIDToken();
	
	void setIDToken(String idToken);

	/**
	 * The access token used to access services.
	 * 
	 * @return the access token
	 */
	String getAccessToken();

	void setAccessToken(String accessToken);

	/**
	 * The refresh token used to renew tokens.
	 * 
	 * @return the refresh token
	 */
	String getRefreshToken();

	void setRefreshToken(String refreshToken);

	/**
	 * The expiry time for the access token.
	 * 
	 * @return the access token expiry time
	 */
	long getAccessTokenExpiryTime();

	void setAccessTokenExpiryTime(long accessTokenExpiryTime);

	/**
	 * The expiry time for the refresh token.
	 * 
	 * @return the access token refresh time
	 */
	long getRefreshTokenExpiryTime();

	void setRefreshTokenExpiryTime(long refreshTokenExpiryTime);

	/**
	 * The time the tokens have been refreshed.
	 * 
	 * @return the refresh time
	 */
	long getLastRefreshedTime();

	void setLastRefreshedTime(long lastRefreshTime);

  /**
   * @param tokenType the request token type
   * @return
   */
  String getToken(int tokenType);
}
