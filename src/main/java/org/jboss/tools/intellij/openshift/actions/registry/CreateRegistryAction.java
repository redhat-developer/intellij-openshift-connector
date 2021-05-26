/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.registry;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistriesNode;
import org.jboss.tools.intellij.openshift.ui.registry.CreateRegistryDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateRegistryAction extends OdoAction {
    public CreateRegistryAction() {
        super(DevfileRegistriesNode.class);
    }

    @Override
    protected String getTelemetryActionName() { return "create registry"; }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
        DevfileRegistriesNode registriesNode = (DevfileRegistriesNode) selected;
        CreateRegistryDialog dialog = new CreateRegistryDialog();
        dialog.show();
        if (dialog.isOK()) {
            ExecHelper.submit(() -> {
                try {
                    odo.createDevfileRegistry(dialog.getName(), dialog.getURL(), dialog.getToken());
                    ((ApplicationsTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(registriesNode);
                    sendTelemetryResults(TelemetryResult.SUCCESS);
                } catch (IOException e) {
                    sendTelemetryError(e);
                    UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create URL"));
                }
            });
        } else {
            sendTelemetryResults(TelemetryResult.ABORTED);
        }
    }
}
