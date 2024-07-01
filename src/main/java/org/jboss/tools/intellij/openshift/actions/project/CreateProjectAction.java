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
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.getRoot;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateProjectAction extends LoggedInClusterAction {


  public static void execute(ApplicationsRootNode rootNode) {
    Odo odo = rootNode.getOdo().getNow(null);
    if (odo == null) {
      return;
    }
    CreateProjectAction action = ActionUtils.createAction(CreateProjectAction.class.getName());
    action.doActionPerformed(null, odo, rootNode.getProject());
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    if (e.getPresentation().isVisible()) {
      Odo odo = getOdo(e);
      if (odo == null) {
        return;
      }
      // overrides label given in plugin.xml
      e.getPresentation().setText("New " + odo.getNamespaceKind());
    }
  }

  @Override
  public String getTelemetryActionName() {
    return "create project";
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    Point location = ActionUtils.getLocation(anActionEvent);
    doActionPerformed(location, odo, getEventProject(anActionEvent));
  }

  private void doActionPerformed(final Point location, @NotNull final Odo odo, Project project) {
    String kind = odo.getNamespaceKind();
    runWithProgress((ProgressIndicator progress) ->
        CompletableFuture
          .supplyAsync(() -> {
            try {
              return odo.getNamespaces();
            } catch (IOException e) {
              NotificationUtils.notifyError("Create New " + kind, "Could not get " + kind.toLowerCase() + "s: " + e.getMessage());
              sendTelemetryError(e.getMessage());
              throw new CompletionException(e);
            }
          }, SwingUtils.EXECUTOR_BACKGROUND)
          .handleAsync((allProjects, error) -> {
              if (error != null) {
                return null;
              }
              CreateNewProjectDialog dialog = openCreateProjectDialog(allProjects, kind, location, project);
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
      "Create Active " + kind + "...",
      project);
  }

  private void createProject(String newProject, @NotNull Odo odo) {
    String kind = odo.getNamespaceKind();
    Notification notification = NotificationUtils.notifyInformation("Create " + kind, "Creating " + kind.toLowerCase() + " " + newProject);
    try {
      odo.createProject(newProject);
      notification.expire();
      NotificationUtils.notifyInformation("Create " + kind, kind + " " + newProject + " successfully created");
      sendTelemetryResults(TelemetryResult.SUCCESS);
    } catch (IOException | CompletionException e) {
      notification.expire();
      sendTelemetryError(e);
      UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create " + kind));
    }
  }

  protected CreateNewProjectDialog openCreateProjectDialog(List<String> allProjects, String kind, Point location, Project project) {
    CreateNewProjectDialog dialog = new CreateNewProjectDialog(project, allProjects, kind, location);
    dialog.show();
    return dialog;
  }

  @Override
  public boolean isVisible(Object selected) {
    return isRoot(selected)
      && isLoggedIn(selected);
  }

  private boolean isLoggedIn(Object node) {
    ApplicationsRootNode root = getRoot(node);
    if (root == null) {
      return false;
    }
    return root.isLogged();
  }

  private boolean isRoot(Object node) {
    return node instanceof ApplicationsRootNode;
  }
}
