/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DeleteProjectAction extends OdoAction {
  public DeleteProjectAction() {
    super(NamespaceNode.class);
  }

  @Override
  public String getTelemetryActionName() { return "delete project"; }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
    NamespaceNode namespaceNode = (NamespaceNode) selected;
    if (Messages.NO == Messages.showYesNoDialog("Delete Project '" + namespaceNode.getName() + "'.\nAre you sure?", "Delete Project",
        Messages.getQuestionIcon())) {
        sendTelemetryResults(TelemetryResult.ABORTED);
        return;
    }
    runWithProgress((ProgressIndicator progress) -> {
        try {
          Notification notif = NotificationUtils.notifyInformation("Delete project", "Deleting project " + namespaceNode.getName());
          Notifications.Bus.notify(notif);
          odo.deleteProject(namespaceNode.getName());
          notif.expire();
          NotificationUtils.notifyInformation("Delete project", "Project " + namespaceNode.getName() + " has been successfully deleted");
          NodeUtils.fireRemoved(namespaceNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
        } catch (IOException e) {
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete Project"));
        }
      },
      "Delete Project " + namespaceNode.getName(),
      getEventProject(anActionEvent)
    );
  }
}
