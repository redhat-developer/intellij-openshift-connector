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
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jboss.tools.intellij.openshift.ui.project.CreateNewProjectDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.getApplicationRootNode;
import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateProjectAction extends LoggedInClusterAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateProjectAction.class);

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if (e.getPresentation().isVisible()) {
            try {
                Odo odo = getOdo(e);
                if (odo == null) {
                    return;
                }
                if (!odo.isOpenShift()) {
                    e.getPresentation().setText("New Namespace");
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("Could not update %s", getApplicationRootNode(e).getProject().getName()), e);
            }
        }
    }

    @Override
  public String getTelemetryActionName() { return "create project"; }

  public static void execute(ApplicationsRootNode rootNode) {
    Odo odo = rootNode.getOdo().getNow(null);
    if (odo == null) {
      return;
    }
    CreateProjectAction action = ActionUtils.createAction(CreateProjectAction.class.getName());
    action.doActionPerformed(null, odo, rootNode.getProject());
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
    Point location = ActionUtils.getLocation(anActionEvent);
    doActionPerformed(location, odo, getEventProject(anActionEvent));
  }

  private void doActionPerformed(final Point location, final Odo odo, Project project) {
    runWithProgress((ProgressIndicator progress) ->
        CompletableFuture
          .supplyAsync(() -> {
            try {
              return odo.getNamespaces();
            } catch (IOException e) {
              NotificationUtils.notifyError("Create New Project", "Could not get projects: " + e.getMessage());
              sendTelemetryError(e.getMessage());
              throw new CompletionException(e);
            }
          }, SwingUtils.EXECUTOR_BACKGROUND)
          .handleAsync((allProjects, error) -> {
              if (error != null) {
                return null;
              }
              CreateNewProjectDialog dialog = openCreateProjectDialog(allProjects, location, project);
              if (dialog.isOK()) {
                return dialog.getNewProject();
              } else {
                sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
                return null;
              }
            }
            , SwingUtils.EXECUTOR_UI)
          .whenCompleteAsync((newProject, error) -> {
            if (error != null
              || newProject == null) {
              return;
            }
            createProject(newProject, odo);
          }, SwingUtils.EXECUTOR_BACKGROUND),
      "Create Active Project...",
    project);
  }

  private void createProject(String newProject, Odo odo) {
    Notification notification = NotificationUtils.notifyInformation("Create Project", "Creating project " + newProject);
    try {
        odo.createProject(newProject);
        notification.expire();
        NotificationUtils.notifyInformation("Create Project", "Project " + newProject + " successfully created");
        sendTelemetryResults(TelemetryResult.SUCCESS);
      } catch (IOException | CompletionException e) {
        notification.expire();
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create Project"));
      }
  }

  private CreateNewProjectDialog openCreateProjectDialog(List<String> allProjects, Point location, Project project) {
    CreateNewProjectDialog dialog = new CreateNewProjectDialog(project, allProjects, location);
    dialog.show();
    return dialog;
  }

}
