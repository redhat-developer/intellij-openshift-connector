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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jboss.tools.intellij.openshift.actions.ActionUtils;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jboss.tools.intellij.openshift.ui.project.ChangeActiveProjectDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.getApplicationRootNode;
import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;

public class ChangeActiveProjectAction extends OdoAction {

  public ChangeActiveProjectAction() {
    super();
  }

  public static void execute(ParentableNode<?> node) {
    ApplicationsRootNode rootNode = node.getRoot();
    Odo odo = rootNode.getOdo().getNow(null);
    if (odo == null) {
      return;
    }
    Point location = MouseInfo.getPointerInfo().getLocation();
    Project project = rootNode.getProject();
    ChangeActiveProjectAction action = ActionUtils.createAction(ChangeActiveProjectAction.class.getName());
    action.doActionPerformed(rootNode, location, odo, project);
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
    Project project = getEventProject(anActionEvent);
    Point location = ActionUtils.getLocation(anActionEvent);
    ApplicationsRootNode rootNode = getApplicationRootNode(anActionEvent);
    doActionPerformed(rootNode, location, odo, project);
  }

  private void doActionPerformed(final ApplicationsRootNode rootNode, final Point location, final @NotNull Odo odo, Project project) {
    runWithProgress((ProgressIndicator progress) ->
        CompletableFuture
          .supplyAsync(() -> {
            try {
              return new ClusterProjects(odo.isOpenShift(), odo.getCurrentNamespace(), odo.getNamespaces());
            } catch (IOException e) {
              NotificationUtils.notifyError("Change Active Project", "Could not get projects: " + e.getMessage());
              sendTelemetryError(e.getMessage());
              throw new RuntimeException(e);
            }
          }, SwingUtils.EXECUTOR_BACKGROUND)
          .handleAsync((clusterProjects, error) -> {
              if (error != null) {
                return null;
              }
              ChangeActiveProjectDialog dialog = openActiveProjectDialog(clusterProjects.isOpenShift, clusterProjects.current, clusterProjects.all, location, project);
              if (dialog.isOK()) {
                return new ChangeActiveProjectOperation(dialog.getActiveProject(), odo);
              } else if (dialog.isCreateNewProject()) {
                // create new project link clicked
                return new CreateActiveProjectOperation(rootNode);
              } else {
                sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
                return null;
              }
            }
            , SwingUtils.EXECUTOR_UI)
          .whenCompleteAsync((operation, error) -> {
            if (error != null
              || operation == null) {
              return;
            }
            operation.run();
          }, SwingUtils.EXECUTOR_BACKGROUND),
      "Change Active Project...",
      project);
  }

  private ChangeActiveProjectDialog openActiveProjectDialog(boolean isOpenShift, String currentProject, List<String> allProjects, Point location, Project project) {
    String kind = isOpenShift ? "Project" : "Namespace";
    ChangeActiveProjectDialog dialog = new ChangeActiveProjectDialog(project, kind, currentProject, allProjects, location);
    dialog.show();
    return dialog;
  }

  @Override
  public String getTelemetryActionName() {
    return "change active project";
  }

  @Override
  public boolean isVisible(Object selected) {
    return (selected instanceof NamespaceNode)
      || ((selected instanceof ApplicationsRootNode && ((ApplicationsRootNode) selected).isLogged()));
  }

  private static final class ClusterProjects {

    private final boolean isOpenShift;
    private final String current;
    private final List<String> all;

    public ClusterProjects(boolean isOpenShift, String current, List<String> all) {
      this.isOpenShift = isOpenShift;
      this.current = current;
      this.all = all;
    }
  }

  private final class ChangeActiveProjectOperation implements Runnable {

    private final Odo odo;
    private final String activeProject;

    private ChangeActiveProjectOperation(String activeProject, Odo odo) {
      this.activeProject = activeProject;
      this.odo = odo;
    }

    @Override
    public void run() {
      try {
        odo.setProject(activeProject);
        NotificationUtils.notifyInformation("Change active project", "Active project set to '" + activeProject + "'");
        sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
      } catch (IOException e) {
        NotificationUtils.notifyError("Change active project", "Could not set active project: " + e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  private final class CreateActiveProjectOperation implements Runnable {

    private final ApplicationsRootNode root;

    public CreateActiveProjectOperation(ApplicationsRootNode root) {
      this.root = root;
    }

    @Override
    public void run() {
      sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
      ApplicationManager.getApplication().invokeLater(() ->
        CreateProjectAction.execute(root)
      );
    }
  }
}
