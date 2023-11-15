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
package org.jboss.tools.intellij.openshift.actions.namespace;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jboss.tools.intellij.openshift.ui.cluster.SelectResourceDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;

public class SelectCurrentNamespaceAction extends OdoAction {
  public SelectCurrentNamespaceAction() {
    super(NamespaceNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
    if (selected == null) {
      return;
    }
    Project project = getEventProject(anActionEvent);
    runWithProgress((ProgressIndicator progress) -> {
        CompletableFuture
          .supplyAsync(() -> {
            try {
              return Arrays.asList(odo.isOpenShift(), odo.getNamespace(), odo.getNamespaces());
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }, SwingUtils.EXECUTOR_BACKGROUND)
          .whenCompleteAsync((list, error) -> {
              if (error == null) {
                boolean isOpenShift = (boolean) list.get(0);
                String currentNamespace = (String) list.get(1);
                List<String> allNamespaces = (List<String>) list.get(2);
                Point location = getLocation(anActionEvent);
                openSelectNamespaceDialog(isOpenShift, currentNamespace, allNamespaces, location, project);
              }
            }
          , SwingUtils.EXECUTOR_UI);
        sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
      },
      "Refreshing...",
      project);
  }

  private static Point getLocation(AnActionEvent anActionEvent) {
    MouseEvent event = ((MouseEvent) anActionEvent.getInputEvent());
      if (event == null) {
        return null;
      }
      return event.getLocationOnScreen();
  }

  private void openSelectNamespaceDialog(boolean isOpenShift, String currentNamespace, List<String> allNamespaces, Point location, Project project) {
    String kind = isOpenShift? "Project" : "Namespace";
    new SelectResourceDialog<>(
      project, kind, currentNamespace, allNamespaces, Function.identity(), this::onNamespaceSelected, location)
      .show();
  }

  private void onNamespaceSelected(String namespace) {
    System.err.println(namespace);
  }

  @Override
  protected String getTelemetryActionName() {
    return "set current namespace";
  }
}
