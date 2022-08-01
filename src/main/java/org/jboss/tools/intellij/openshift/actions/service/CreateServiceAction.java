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
package org.jboss.tools.intellij.openshift.actions.service;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.ui.service.CreateServiceDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateServiceAction extends OdoAction {

    public CreateServiceAction() {
        super(NamespaceNode.class);
    }

    @Override
    protected String getTelemetryActionName() { return "create service"; }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
        String projectName;
        projectName = ((NamespaceNode)selected).getName();
        CompletableFuture.runAsync(() -> {
            try {
                List<ServiceTemplate> templates = odo.getServiceTemplates();
                if (!templates.isEmpty()) {
                    CreateServiceDialog dialog = UIHelper.executeInUI(() -> showDialog(templates));
                    if (dialog.isOK()) {
                        createService(odo, projectName, dialog);
                        ((ApplicationsTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(selected);
                        sendTelemetryResults(TelemetryResult.SUCCESS);
                    } else {
                        sendTelemetryResults(TelemetryResult.ABORTED);
                    }
                } else {
                    String message = "No templates available";
                    sendTelemetryError(message);
                    UIHelper.executeInUI(() -> Messages.showWarningDialog(message, "Create service"));
                }
            } catch (IOException e) {
                sendTelemetryError(e);
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create service"));
            }
        });
    }

    private void createService(Odo odo, String project, CreateServiceDialog dialog) throws IOException {
        odo.createService(project, dialog.getServiceTemplate(), dialog.getServiceTemplateCRD(),
                dialog.getName(), dialog.getSpec(), false);
    }

    protected CreateServiceDialog showDialog(List<ServiceTemplate> templates) {
        CreateServiceDialog dialog = new CreateServiceDialog();
        dialog.setServiceTemplates(templates.toArray(new ServiceTemplate[templates.size()]));
        dialog.show();
        return dialog;
    }
}
