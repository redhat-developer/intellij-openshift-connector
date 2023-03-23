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

import com.intellij.openapi.components.Service;
import org.jboss.tools.intellij.openshift.oauth.exception.OAuthException;

/**
 * Delegates to the account service as we don't control the lifecycle of a token provider
 */
@Service
public final class DefaultTokenProvider implements TokenProvider {

	@Override
	public String getToken(String serverId, int tokenType, Object context) throws OAuthException {
	  return AccountService.getDefault().getToken(serverId, tokenType, context);
	}
}
