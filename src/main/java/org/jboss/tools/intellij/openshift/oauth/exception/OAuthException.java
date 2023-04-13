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

public abstract class OAuthException extends RuntimeException {

	private static final long serialVersionUID = -66495414279364584L;

	protected OAuthException(String message) {
		super(message);
	}

	protected OAuthException(String message, Throwable cause) {
		super(message, cause);
	}

}
