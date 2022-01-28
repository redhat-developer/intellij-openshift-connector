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

public final class OAuthCoreConstants {

	private OAuthCoreConstants() {
	}

	public static final String TOKEN_PROVIDER_EXTENSION_POINT = "org.jboss.tools.common.oauth.core.tokenProvider";
	
	public static final String AUTHORIZATION_SERVER_EXTENSION_POINT = "org.jboss.tools.common.oauth.core.authorizationServer";

	public static final String LOGIN_PROVIDER_EXTENSION_POINT = "org.jboss.tools.common.oauth.core.loginProvider";

	public static final String ACCOUNT_BASE_KEY = "accounts";

	public static final long DURATION_24_HOURS = 24 * 3600 * 1000L;

  public static final String ID_ATTRIBUTE_NAME = "id";

  public static final String REALM_ATTRIBUTE_NAME = "realm";

  public static final String URL_ATTRIBUTE_NAME = "url";

  public static final String CLIENT_ID_ATTRUBUTE_NAME = "client-id";

  public static final String DISPLAY_NAME_ATTRUBUTE_NAME = "displayName";

  public static final String REFRESH_SUFFIX = "/token/refresh";
  
  public static final String REDHAT_SSO_SERVER_ID = "org.jboss.tools.common.oauth.core.authorizationServer.redhat";


}
