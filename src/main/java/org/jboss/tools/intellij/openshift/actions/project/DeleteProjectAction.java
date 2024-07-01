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
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DeleteProjectAction extends OdoAction {
  public DeleteProjectAction() {
    super(NamespaceNode.class);
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    if (e.getPresentation().isVisible()) {
      OdoFacade odo = getOdo(e);
      if (odo == null) {
        return;
      }
      // overrides label given in plugin.xml
      e.getPresentation().setText("Delete " + odo.getNamespaceKind());
    }
  }

  @Override
  public String getTelemetryActionName() {return "delete project";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    String kind = odo.getNamespaceKind();
    NamespaceNode namespaceNode = (NamespaceNode) selected;
    if (Messages.NO == Messages.showYesNoDialog("Delete " + kind + " '" + namespaceNode.getName() + "'.\n\nDELETING " + kind.toUpperCase() + " WILL DELETE ALL ASSOCIATED RESOURCES ON THE CLUSTER.\n\nAre you sure?", "Delete " + kind,
      Messages.getQuestionIcon())) {
      sendTelemetryResults(TelemetryResult.ABORTED);
      return;
    }
    runWithProgress((ProgressIndicator progress) -> {
        try {
          Notification notif = NotificationUtils.notifyInformation("Delete " + kind, "Deleting " + kind.toLowerCase() + " " + namespaceNode.getName());
          Notifications.Bus.notify(notif);
          odo.deleteProject(namespaceNode.getName());
          notif.expire();
          NotificationUtils.notifyInformation("Delete " + kind, kind + " " + namespaceNode.getName() + " has been successfully deleted");
          NodeUtils.fireRemoved(namespaceNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
        } catch (IOException e) {
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete " + kind));
        }
      },
      "Delete " + kind + " " + namespaceNode.getName(),
      getEventProject(anActionEvent)
    );
  }
}
