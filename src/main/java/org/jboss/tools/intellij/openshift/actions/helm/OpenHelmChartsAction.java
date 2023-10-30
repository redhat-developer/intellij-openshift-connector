/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.helm;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jboss.tools.intellij.openshift.actions.HelmAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.helm.ChartsDialog;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.NotNull;

public class OpenHelmChartsAction extends HelmAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Object selected, @NotNull Helm helm) {
        Project project = getEventProject(anActionEvent);
        if (!(selected instanceof ApplicationsRootNode)) {
            return;
        }
        ChartsDialog dialog = new ChartsDialog((ApplicationsRootNode) selected, project);
        dialog.show();
    }

    @Override
    protected String getTelemetryActionName() {
        return "helm-charts";
    }

    @Override
    public boolean isVisible(Object selected) {
        return selected.getClass() == ApplicationsRootNode.class;
    }
}
