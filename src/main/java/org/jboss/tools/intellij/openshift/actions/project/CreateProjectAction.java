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
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.ActionUtils;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.actions.cluster.LoggedInClusterAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateProjectAction extends LoggedInClusterAction {

  @Override
  public String getTelemetryActionName() { return "create project"; }

  public static void execute(ApplicationsRootNode rootNode) {
    Odo odo = rootNode.getOdo().getNow(null);
    if (odo == null) {
      return;
    }
    CreateProjectAction action = ActionUtils.createAction(CreateProjectAction.class.getName());
    action.doActionPerformed(rootNode, odo, rootNode.getProject());
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) selected;
    doActionPerformed(clusterNode, odo, getEventProject(anActionEvent));
  }

  private void doActionPerformed(ApplicationsRootNode clusterNode, Odo odo, Project project) {
    String projectName = Messages.showInputDialog("Project name", "New Project", Messages.getQuestionIcon());
    if ((projectName == null) || projectName.trim().isEmpty()) {
      sendTelemetryResults(TelemetryResult.ABORTED);
      return;
    }
    runWithProgress((ProgressIndicator progress) -> {
      try {
        Notification notif = NotificationUtils.notifyInformation("Create Project", "Creating project " + projectName);
        odo.createProject(projectName);
        notif.expire();
        NotificationUtils.notifyInformation("Create Project", "Project " + projectName + " successfully created");
        clusterNode.getStructure().fireModified(clusterNode);
        sendTelemetryResults(TelemetryResult.SUCCESS);
      } catch (IOException | CompletionException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create Project"));
      }
    },
    "Create Project...",
    project);
  }
}
