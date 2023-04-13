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
package org.jboss.tools.intellij.openshift.oauth.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.Service;
import org.jboss.tools.intellij.openshift.oauth.LoginProvider;
import org.jboss.tools.intellij.openshift.oauth.LoginResponse;
import org.jboss.tools.intellij.openshift.oauth.OAuthUtils;
import org.jboss.tools.intellij.openshift.oauth.model.IAuthorizationServer;
import org.keycloak.adapters.KeycloakDeployment;

import java.awt.Frame;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Login provider that will launch a browser to perform the login and extract
 * the JSON.
 * 
 */
@Service
public final class DefaultLoginProvider implements LoginProvider {

	private static final int TIMEOUT_JOB_ON_UI_THREAD = 5 * 60 * 1000;

	@Override
	public LoginResponse login(IAuthorizationServer server, Object context) {
		if (ApplicationManager.getApplication().isDispatchThread()) {
			return loginInUI(server, context);
		} else {
			return runInJob(server, context);
		}
	}

	LoginResponse runInJob(IAuthorizationServer server, Object context) {
		CompletableFuture<LoginResponse> result = new CompletableFuture<>();
		ApplicationManager.getApplication().invokeLater(() -> {
			result.complete(loginInUI(server, context));
		}, ModalityState.any());
		try {
			return result.get(TIMEOUT_JOB_ON_UI_THREAD, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} catch (ExecutionException | TimeoutException e) {
			return null;
		}
	}
	
	public LoginResponse loginInUI(IAuthorizationServer server, Object context) {
		KeycloakDeployment deployment = OAuthUtils.getDeployment(server);
		BrowserBasedLoginDialog dialog = new BrowserBasedLoginDialog((Frame) context, deployment);
		dialog.pack();
		dialog.setVisible(true);
		return dialog.getInfo();
	}
}
