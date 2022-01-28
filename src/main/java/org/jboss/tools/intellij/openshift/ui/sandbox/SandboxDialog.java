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
import com.intellij.ui.wizard.WizardDialog;

import java.awt.Dimension;
import java.awt.Frame;

public class SandboxDialog extends WizardDialog<SandboxModel> {
    private final Project project;

    public SandboxDialog(Project project, boolean canBeParent, SandboxModel model) {
        super(project, canBeParent, model);
        this.project = project;
    }

    @Override
    protected Dimension getWindowPreferredSize() {
        Frame f = WindowManager.getInstance().getFrame(project);
        return new Dimension(f.getWidth() * 3 / 4, f.getHeight() * 3 / 4);
    }

    @Override
    public void onStepChanged() {
        super.onStepChanged();
        if (myModel.getCurrentStep() instanceof SandboxLoginPage) {
            getRootPane().setDefaultButton(null);
        }
    }
}
