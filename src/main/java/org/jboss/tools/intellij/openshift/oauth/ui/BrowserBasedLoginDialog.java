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
import com.intellij.openapi.application.ModalityState;
import org.jboss.tools.intellij.openshift.oauth.LoginResponse;
import org.keycloak.adapters.KeycloakDeployment;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BrowserBasedLoginDialog extends JDialog {
    private LoginResponse info;
    private JPanel contentPane;
    private JButton buttonCancel;
    private OAuthBrowser browser;

    public BrowserBasedLoginDialog(Frame owner, KeycloakDeployment deployment) {
        super(owner);
        setContentPane(contentPane);

        Dimension ownerSize = getOwner().getSize();
        setPreferredSize(new Dimension(ownerSize.width * 3 / 4, ownerSize.height * 3 / 4));

        setModal(true);

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        browser.getRedirectFuture().handle((response, t) -> {
           info = response;
            ApplicationManager.getApplication().invokeLater(this::dispose, ModalityState.any());
            return null;
        });
        browser.setDeployment(deployment);
    }

    public void createUIComponents() {
        browser = new OAuthBrowser();
    }

    private void onCancel() {
        dispose();
    }

    /**
     * @return the info
     */
    public LoginResponse getInfo() {
        return info;
    }
}
