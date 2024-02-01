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
package org.jboss.tools.intellij.openshift.oauth.exception;

import org.jboss.tools.intellij.openshift.oauth.model.IAccount;

public class OAuthRefreshException extends OAuthException {

	private final IAccount account;
	private final int status;

	public static final int NO_STATUS = -1;

	public OAuthRefreshException(IAccount account, Throwable t) {
		super("Error refreshing token for " + account.getId() + " on autorization server " + account.getAuthorizationServer().getId(), t);
		this.account = account;
		this.status = NO_STATUS;
	}

	/**
	 * @return the account
	 */
	public IAccount getAccount() {
		return account;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
}
