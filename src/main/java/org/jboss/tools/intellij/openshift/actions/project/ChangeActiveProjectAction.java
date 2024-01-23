/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jboss.tools.intellij.openshift.actions.ActionUtils;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jboss.tools.intellij.openshift.ui.cluster.ChangeActiveProjectDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;

public class ChangeActiveProjectAction extends OdoAction {

  public ChangeActiveProjectAction() {
    super(NamespaceNode.class, ApplicationsRootNode.class);
  }

  public static void execute(ParentableNode<?> parentNode) {
    ChangeActiveProjectAction action = (ChangeActiveProjectAction) ActionManager.getInstance().getAction(ChangeActiveProjectAction.class.getName());
    NamespaceNode namespaceNode = (NamespaceNode) parentNode;
    action.telemetrySender = new TelemetrySender(PREFIX_ACTION + action.getTelemetryActionName());
    Odo odo = namespaceNode.getRoot().getOdo().getNow(null);
    if (odo == null) {
      return;
    }
    action.actionPerformedOnSelectedObject(null, null, odo);
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
    Project project = getEventProject(anActionEvent);
    runWithProgress((ProgressIndicator progress) -> {
        CompletableFuture
          .supplyAsync(() -> {
            try {
              return new ClusterProjects(odo.isOpenShift(), odo.getCurrentNamespace(), odo.getNamespaces());
            } catch (IOException e) {
              NotificationUtils.notifyError("Change active project", "Could not get projects: " + e.getMessage());
              sendTelemetryError(e.getMessage());
              throw new RuntimeException(e);
            }
          }, SwingUtils.EXECUTOR_BACKGROUND)
          .handleAsync((ClusterProjects, error) -> {
              if (error != null) {
                return null;
              }
              Point location = ActionUtils.getLocation(anActionEvent);
              return openActiveProjectDialog(ClusterProjects.isOpenShift, ClusterProjects.current, ClusterProjects.all, location, project);
           }
            , SwingUtils.EXECUTOR_UI)
          .whenCompleteAsync((activeProject, error) -> {
            if (error == null
              && activeProject != null) {
              try {
                odo.setProject(activeProject);
                NotificationUtils.notifySuccess("Change active project", "Active project set to '" + activeProject + "'");
                sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
              } catch (IOException e) {
                NotificationUtils.notifyError("Change active project", "Could not set active project: " + e.getMessage());
                throw new RuntimeException(e);
              }
            }
          }, SwingUtils.EXECUTOR_BACKGROUND);
      },
      "Change Active Project...",
      project);
  }

  private String openActiveProjectDialog(boolean isOpenShift, String currentProject, List<String> allProjects, Point location, Project project) {
    String kind = isOpenShift ? "Project" : "Namespace";
    ChangeActiveProjectDialog dialog = new ChangeActiveProjectDialog(project, kind, currentProject, allProjects, location);
    if (dialog.showAndGet()) {
      return dialog.getActiveProject();
    } else {
      return null;
    }
  }

  @Override
  protected String getTelemetryActionName() {
    return "change active project";
  }

  private static class ClusterProjects {

    private final boolean isOpenShift;
    private final String current;
    private final List<String> all;

    public ClusterProjects(boolean isOpenShift, String current, List<String> all) {
      this.isOpenShift = isOpenShift;
      this.current = current;
      this.all = all;
    }
  }
}
