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

import com.intellij.openapi.application.ApplicationManager;
import org.jboss.tools.intellij.openshift.oauth.model.IAccount;
import org.jboss.tools.intellij.openshift.oauth.model.IAuthorizationServer;
import org.jboss.tools.intellij.openshift.oauth.ui.DefaultLoginProvider;

/**
 * A login provider responsible for performing login to OAuth authorization server.
 * This involves UI.
 */
public interface LoginProvider {
	LoginResponse login(IAuthorizationServer server, IAccount account, Object context);

	static LoginProvider get() {
		return ApplicationManager.getApplication().getService(DefaultLoginProvider.class);
	}
}
