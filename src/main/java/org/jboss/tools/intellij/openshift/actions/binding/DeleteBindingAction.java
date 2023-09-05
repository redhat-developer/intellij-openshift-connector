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
package org.jboss.tools.intellij.openshift.actions.binding;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.BindingNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;

public class DeleteBindingAction extends OdoAction {
    public DeleteBindingAction() {
        super(BindingNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
        try {
            BindingNode node = (BindingNode) selected;
            if (Messages.NO == Messages.showYesNoDialog("Delete binding '" + node.getName() + "'.\nAre you sure?",
                    "Delete binding",
                    Messages.getQuestionIcon())) {
                sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
                return;
            }
            odo.deleteBinding(node.getParent().getNamespace(), node.getParent().getComponent().getPath(),
                    node.getParent().getComponent().getName(),node.getBinding().getName());
            NodeUtils.fireModified(node.getParent());
            sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
        } catch (IOException e) {
            sendTelemetryError(e);
            Messages.showWarningDialog("Error: " + e.getLocalizedMessage(), "Delete binding");
        }
    }

    @Override
    protected String getTelemetryActionName() {
        return "delete binding";
    }
}
