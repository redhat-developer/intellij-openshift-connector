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
package org.jboss.tools.intellij.openshift.actions.helm;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;
import org.jboss.tools.intellij.openshift.actions.HelmAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoryNode;
import org.jboss.tools.intellij.openshift.tree.application.ProcessingNode;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.Nullable;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;

public class RemoveRepositoriesAction extends HelmAction {

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected) {
    Helm helm = getHelm(anActionEvent);
    if (helm == null) {
      return;
    }
    Project project = getEventProject(anActionEvent);
    List<HelmRepositoryNode> repositories = toHelmRepositoryNodes(selected);
    if (repositories == null
      || repositories.isEmpty()) {
      return;
    }

    if (cancelRemoval(repositories)) {
      sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
      return;
    }

    runWithProgress((ProgressIndicator progress) -> removeRepositories(repositories, helm),
      "Remove Helm Repositories...",
      project);
  }

  private void removeRepositories(List<HelmRepositoryNode> repositories, Helm helm) {
    ProcessingNode[] processingNodes = repositories.toArray(new ProcessingNode[0]);
    ApplicationsRootNode rootNode = repositories.get(0).getRoot();
    try {
      setProcessing("removing...", rootNode, processingNodes);
      helm.removeRepos(
        repositories.stream()
        .map(HelmRepositoryNode::getName)
        .toArray(String[]::new));
      clearProcessing(rootNode, processingNodes);
      sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
    } catch (Exception e) {
      clearProcessing(rootNode, processingNodes);
      UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Remove Helm Repositories"));
      sendTelemetryResults(TelemetryService.TelemetryResult.ERROR);
    }
  }

  @Nullable
  private static List<HelmRepositoryNode> toHelmRepositoryNodes(Object[] selected) {
    if (selected == null
      || selected.length == 0) {
      return null;
    }

    return Arrays.stream(selected)
      .map(StructureTreeAction::getElement)
      .filter(HelmRepositoryNode.class::isInstance)
      .map(HelmRepositoryNode.class::cast)
      .collect(Collectors.toList());
  }

  private boolean cancelRemoval(List<HelmRepositoryNode> releases) {
    String repositoriesNames = releases.stream()
      .map(HelmRepositoryNode::getName)
      .collect(Collectors.joining(", "));
    return Messages.NO == Messages.showYesNoDialog(
      "Remove Repositories "
        + repositoriesNames
        + ".\n\nAre you sure?",
      "Remove Repositories",
      Messages.getQuestionIcon());
  }

  @Override
  public String getTelemetryActionName() {
    return "helm-remove repositories";
  }

  @Override
  public boolean isVisible(Object[] selected) {
    return Arrays.stream(selected).anyMatch(item -> {
      Object node = getElement(item);
      if (!(node instanceof HelmRepositoryNode)) {
        return false;
      }
      return !((HelmRepositoryNode) node).isProcessing();
    });
  }
}
