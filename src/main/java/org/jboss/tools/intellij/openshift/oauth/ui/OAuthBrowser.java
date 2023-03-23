/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.oauth.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jboss.tools.intellij.openshift.oauth.LoginResponse;
import org.keycloak.OAuthErrorException;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.installed.KeycloakInstalled;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessTokenResponse;

import javax.swing.JPanel;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import static org.keycloak.adapters.installed.KeycloakInstalled.DesktopProvider;

public class OAuthBrowser extends JPanel {

    private JBCefBrowser browser;
    private KeycloakInstalled adapter;
    private CompletableFuture<LoginResponse> redirectFuture = new CompletableFuture<>();

    public OAuthBrowser() {
        browser = new JBCefBrowser();
        browser.loadURL("https://www.redhat.com");
        add(browser.getComponent());
    }

    private void processRedirect() {
        try {
            adapter.loginDesktop();
            AccessTokenResponse response = adapter.getTokenResponse();
            LoginResponse info = new LoginResponse();
            info.setIDToken(response.getIdToken());
            info.setAccessToken(response.getToken());
            info.setAccessTokenExpiryTime(System.currentTimeMillis() + response.getExpiresIn() * 1000);
            info.setRefreshToken(response.getRefreshToken());
            info.setRefreshTokenExpiryTime(System.currentTimeMillis() + response.getRefreshExpiresIn() * 1000);
            redirectFuture.complete(info);
        } catch (IOException | VerificationException | OAuthErrorException | URISyntaxException | ServerRequest.HttpFailure | InterruptedException e) {
            redirectFuture.completeExceptionally(e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setDeployment(KeycloakDeployment deployment) {
        adapter = new KeycloakInstalled(deployment);
        adapter.setDesktopProvider(new DesktopProvider() {
            @Override
            public void browse(URI uri) {
                browser.loadURL(uri.toString());
            }
        });
        ApplicationManager.getApplication().executeOnPooledThread(this::processRedirect);
    }

    public CompletableFuture<LoginResponse> getRedirectFuture() {
        return redirectFuture;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (adapter != null) {
            adapter.close();
        }
    }
}
