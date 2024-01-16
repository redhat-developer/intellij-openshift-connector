/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.helm;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.HelmAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ChartReleaseNode;
import org.jboss.tools.intellij.openshift.tree.application.ProcessingNode;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;

public class UninstallReleaseAction extends HelmAction {


  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected) {
    Helm helm = getHelm(anActionEvent);
    if (helm == null) {
      return;
    }
    Project project = getEventProject(anActionEvent);
    List<ChartReleaseNode> releases = toChartReleaseNodes(selected);
    if (releases == null
      || releases.isEmpty()) {
      return;
    }

    if (cancelUninstall(releases)) {
      sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
      return;
    }

    runWithProgress((ProgressIndicator progress) -> {
        ProcessingNode[] processingNodes = releases.toArray(new ProcessingNode[0]);
        ApplicationsRootNode rootNode = releases.get(0).getRoot();
        try {
          setProcessing("uninstalling...", rootNode, processingNodes);
          helm.uninstall(releases.stream().map(ChartReleaseNode::getName).toArray(String[]::new));
          clearProcessing(rootNode, processingNodes);
          sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
        } catch (Exception e) {
          clearProcessing(rootNode, processingNodes);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Uninstall Helm Release"));
          sendTelemetryResults(TelemetryService.TelemetryResult.ERROR);
        }
      },
      "Uninstall Helm Release...",
      project);
  }

  @Nullable
  private static List<ChartReleaseNode> toChartReleaseNodes(Object[] selected) {
    if (selected == null
      || selected.length == 0) {
      return null;
    }

    return Arrays.stream(selected)
      .map(StructureTreeAction::getElement)
      .filter(ChartReleaseNode.class::isInstance)
      .map(ChartReleaseNode.class::cast)
      .collect(Collectors.toList());
  }

  private boolean cancelUninstall(List<ChartReleaseNode> releases) {
    String releaseNames = releases.stream().map(ChartReleaseNode::getName).collect(Collectors.joining(", "));
    return Messages.NO == Messages.showYesNoDialog(
      "Delete Releases "
        + releaseNames
        + ".\nAre you sure?",
      "Delete Releases",
      Messages.getQuestionIcon());
  }

  @Override
  protected String getTelemetryActionName() {
    return "helm-uninstall release";
  }

  @Override
  public boolean isVisible(Object[] selected) {
    return Arrays.stream(selected).anyMatch(item -> {
      Object node = getElement(item);
      if (!(node instanceof ChartReleaseNode)) {
        return false;
      }
      return !((ChartReleaseNode) node).isProcessing();
    });
  }
}
