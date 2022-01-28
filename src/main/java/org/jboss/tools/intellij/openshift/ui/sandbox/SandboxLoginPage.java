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
package org.jboss.tools.intellij.openshift.ui.sandbox;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import org.jboss.tools.intellij.openshift.ui.cluster.OAuthBrowser;
import org.jboss.tools.intellij.openshift.utils.KubernetesClusterHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Frame;
import java.io.IOException;

public class SandboxLoginPage extends WizardStep<SandboxModel> {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxLoginPage.class);

    private final SandboxModel model;
    private final Project project;
    private OAuthBrowser browser;
    private JPanel root;

    public SandboxLoginPage(SandboxModel model, Project project) {
        super("", "Please login to Red Hat SSO if required, then provide required information to bootstrap your Red Hat Developer Sandbox.");
        this.model = model;
        this.project = project;
    }

    public void createUIComponents() {
        browser = new OAuthBrowser();
        browser.addTokenListener(e -> {
            model.setClusterToken(e.getToken());
            model.getCurrentNavigationState().FINISH.setEnabled(true);
        });
        Frame f = WindowManager.getInstance().getFrame(project);
        browser.setSize(f.getWidth() * 3 / 4, f.getHeight() * 3 / 4);

    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
        try {
            state.FINISH.setEnabled(false);
            state.NEXT.setEnabled(false);
            state.PREVIOUS.setEnabled(false);
            browser.setUrl(KubernetesClusterHelper.getTokenRequest(model.getClusterURL()));
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return root;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return browser;
    }

    @Override
    public boolean onFinish() {
        model.setComplete(true);
        return super.onFinish();
    }
}
