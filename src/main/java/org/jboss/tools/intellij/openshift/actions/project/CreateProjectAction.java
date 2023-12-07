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
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.cluster.LoggedInClusterAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import static org.jboss.tools.intellij.openshift.Constants.GROUP_DISPLAY_ID;
import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateProjectAction extends LoggedInClusterAction {

  @Override
  protected String getTelemetryActionName() { return "create project"; }

  public static void execute(ApplicationsRootNode rootNode) {
    CreateProjectAction action = (CreateProjectAction) ActionManager.getInstance().getAction(CreateProjectAction.class.getName());
    action.telemetrySender = new TelemetrySender(PREFIX_ACTION + action.getTelemetryActionName());
    Odo odo = rootNode.getOdo().getNow(null);
    if (odo == null) {
      return;
    }
    action.doActionPerformed(rootNode, odo, rootNode.getProject());
  }
  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) selected;
    doActionPerformed(clusterNode, odo, getEventProject(anActionEvent));
  }

  private void doActionPerformed(ApplicationsRootNode clusterNode, Odo odo, Project project) {
    String projectName = Messages.showInputDialog("Project name", "New project", Messages.getQuestionIcon());
    if ((projectName != null) && projectName.trim().length() > 0) {
      runWithProgress((ProgressIndicator progress) -> {
        try {
          Notification notif = new Notification(GROUP_DISPLAY_ID, "Create project", "Creating project " + projectName, NotificationType.INFORMATION);
          Notifications.Bus.notify(notif);
          odo.createProject(projectName);
          notif.expire();
          Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "Create project", "Project " + projectName + " successfully created", NotificationType.INFORMATION));
          clusterNode.getStructure().fireModified(clusterNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
        } catch (IOException | CompletionException e) {
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create Project"));
        }
      },
      "Create Project...",
      project);
    } else {
      sendTelemetryResults(TelemetryResult.ABORTED);
    }
  }
}
