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
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.HelmAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ChartReleaseNode;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;

public class UninstallReleaseAction extends HelmAction {

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, @NotNull Helm helm) {
    Project project = getEventProject(anActionEvent);
    ChartReleaseNode releaseNode = (ChartReleaseNode) selected;
    if (Messages.NO == Messages.showYesNoDialog(
      "Delete Release '" + releaseNode.getName() + "'.\nAre you sure?",
      "Delete Release",
      Messages.getQuestionIcon())) {
      sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
      return;
    }
    runWithProgress((ProgressIndicator progress) -> {
        try {
          setProcessing("uninstalling...", releaseNode);
          helm.uninstall(releaseNode.getName());
          clearProcessing(releaseNode.getRoot());
          sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
        } catch (IOException e) {
          clearProcessing(releaseNode);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Uninstall Helm Release"));
          sendTelemetryResults(TelemetryService.TelemetryResult.ERROR);
        }
      },
    "Uninstall Helm Release...",
    project);
  }

  @Override
  protected String getTelemetryActionName() {
    return "helm-uninstall release";
  }

  @Override
  public boolean isVisible(Object selected) {
    return selected.getClass() == ChartReleaseNode.class;
  }
}
