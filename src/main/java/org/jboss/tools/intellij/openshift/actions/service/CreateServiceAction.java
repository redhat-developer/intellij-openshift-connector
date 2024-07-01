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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.ui.service.CreateServiceDialog;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;

public class CreateServiceAction extends OdoAction {

  public CreateServiceAction() {
    super(NamespaceNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "create service";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    NamespaceNode namespaceNode = (NamespaceNode) selected;
    if (namespaceNode == null) {
      return;
    }
    runWithProgress((ProgressIndicator progress) -> {
        try {
          List<ServiceTemplate> templates = odo.getServiceTemplates();
          if (templates.isEmpty()) {
            String message = "No templates available";
            sendTelemetryError(message);
            UIHelper.executeInUI(() -> Messages.showWarningDialog(message, "Create Service"));
            return;
          }
          CreateServiceDialog dialog = UIHelper.executeInUI(() -> showDialog(templates));
          if (!dialog.isOK()) {
            sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
            return;
          }
          setProcessing("Creating Service...", namespaceNode);
          odo.createService(namespaceNode.getName(),
            dialog.getServiceTemplate(),
            dialog.getServiceTemplateCRD(),
            dialog.getName(),
            dialog.getSpec(),
            false);
          clearProcessing(namespaceNode);
          sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
        } catch (IOException e) {
          clearProcessing(namespaceNode);
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create Service"));
        }
      },
      "Create Service...",
      getEventProject(anActionEvent));
  }

  protected CreateServiceDialog showDialog(List<ServiceTemplate> templates) {
    CreateServiceDialog dialog = new CreateServiceDialog();
    dialog.setServiceTemplates(templates.toArray(new ServiceTemplate[0]));
    dialog.show();
    return dialog;
  }
}
