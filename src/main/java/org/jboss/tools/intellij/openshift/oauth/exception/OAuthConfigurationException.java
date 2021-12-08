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

public class OAuthConfigurationException extends OAuthException {

	public OAuthConfigurationException() {
	}

	public OAuthConfigurationException(String message) {
		super(message);
	}

	public OAuthConfigurationException(Throwable cause) {
		super(cause);
	}

	public OAuthConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public OAuthConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
