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
package org.jboss.tools.intellij.openshift.oauth;

/**
 * Represent the data stored in the JSON payload returned when OSIO login has been
 * completed.
 */
public class LoginResponse {
  
  private String IDToken;

	private String accessToken;
	
	private long accessTokenExpiryTime;

	private String refreshToken;
	
	private long refreshTokenExpiryTime;

	/**
   * @return the iDToken
   */
  public String getIDToken() {
    return IDToken;
  }

  /**
   * @param iDToken the iDToken to set
   */
  public void setIDToken(String iDToken) {
    IDToken = iDToken;
  }

  /**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
   * @return the accessTokenExpiryTime
   */
  public long getAccessTokenExpiryTime() {
    return accessTokenExpiryTime;
  }

  /**
   * @param accessTokenExpiryTime the accessTokenExpiryTime to set
   */
  public void setAccessTokenExpiryTime(long accessTokenExpiryTime) {
    this.accessTokenExpiryTime = accessTokenExpiryTime;
  }

  /**
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

  /**
   * @return the refreshTokenExpiryTime
   */
  public long getRefreshTokenExpiryTime() {
    return refreshTokenExpiryTime;
  }

  /**
   * @param refreshTokenExpiryTime the refreshTokenExpiryTime to set
   */
  public void setRefreshTokenExpiryTime(long refreshTokenExpiryTime) {
    this.refreshTokenExpiryTime = refreshTokenExpiryTime;
  }
}
