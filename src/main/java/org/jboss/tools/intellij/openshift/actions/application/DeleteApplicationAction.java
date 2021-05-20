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
package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DeleteApplicationAction extends OdoAction {
  public DeleteApplicationAction() {
    super(ApplicationNode.class);
  }

  @Override
  protected String getTelemetryActionName() { return "delete application"; }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
    ApplicationNode applicationNode = (ApplicationNode) selected;
    NamespaceNode namespaceNode = applicationNode.getParent();
    if (Messages.NO == Messages.showYesNoDialog("Delete Application '" + applicationNode.getName() + "'.\nAre you sure?", "Delete Application",
        Messages.getQuestionIcon())) {
        sendTelemetryResults(TelemetryResult.ABORTED);
        return;
    }
    CompletableFuture.runAsync(() -> {
      try {
        odo.deleteApplication(namespaceNode.getName(), applicationNode.getName());
        ((ApplicationsTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireRemoved(applicationNode);
        sendTelemetryResults(TelemetryResult.SUCCESS);
      } catch (IOException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete application"));
      }
    });
  }
}

