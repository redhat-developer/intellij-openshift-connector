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
package org.jboss.tools.intellij.openshift.oauth.exception;

import org.jboss.tools.intellij.openshift.oauth.model.IAccount;
import org.jboss.tools.intellij.openshift.oauth.model.IAuthorizationServer;

public class OAuthLoginException extends OAuthException {

	private final IAccount account;

	public OAuthLoginException(IAuthorizationServer server, IAccount account) {
		super("Error login to authorization server " + server.getDisplayName());
		this.account = account;
	}

	/**
	 * @return the account
	 */
	public IAccount getAccount() {
		return account;
	}
}
