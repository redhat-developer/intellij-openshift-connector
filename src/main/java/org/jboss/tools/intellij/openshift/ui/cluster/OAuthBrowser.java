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
package org.jboss.tools.intellij.openshift.ui.cluster;

import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBuilder;
import com.intellij.ui.jcef.JBCefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefRequest;

import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

public class OAuthBrowser extends JPanel implements CefLoadHandler {
    public class TokenEvent extends EventObject {

        private String token;

        /**
         * @param browser the browser
         * @param token the token to set
         */
        public TokenEvent(OAuthBrowser browser, String token) {
            super(browser);
            this.token = token;
        }

        /**
         * @return the token
         */
        public String getToken() {
            return token;
        }
    }

    public interface TokenListener extends EventListener {
        void tokenReceived(TokenEvent event);
    }

    private JBCefClient client = JBCefApp.getInstance().createClient();
    private JBCefBrowser browser;

    private List<TokenListener> listeners = new ArrayList<>();

    public OAuthBrowser() {
        browser = new JBCefBrowserBuilder().setClient(client).setUrl("https://www.redhat.com").build();
        add(browser.getComponent());
        client.addLoadHandler(this, browser.getCefBrowser());
    }

    public void setText(String text) {
        browser.loadHTML(text);
    }

    public void setUrl(String url) {
        browser.loadURL(url);
    }

    public void addTokenListener(TokenListener listener) {
        listeners.add(listener);
    }

    protected void fireTokenReceived(String token) {
        TokenEvent event = new TokenEvent(this, token);
        listeners.forEach(l -> l.tokenReceived(event));
    }

    @Override
    public void onLoadingStateChange(CefBrowser cefBrowser, boolean b, boolean b1, boolean b2) {
    }

    @Override
    public void onLoadStart(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest.TransitionType transitionType) {
    }

    @Override
    public void onLoadEnd(CefBrowser cefBrowser, CefFrame cefFrame, int i) {
        cefBrowser.getSource(s -> {
            TokenExtractor extractor = new TokenExtractor(s);
            if (extractor.isTokenPage()) {
                fireTokenReceived(extractor.getToken());
            }
        });
    }

    @Override
    public void onLoadError(CefBrowser cefBrowser, CefFrame cefFrame, ErrorCode errorCode, String s, String s1) {
    }
}
