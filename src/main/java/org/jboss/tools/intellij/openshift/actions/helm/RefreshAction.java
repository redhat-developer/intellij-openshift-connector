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
import org.jboss.tools.intellij.openshift.actions.HelmAction;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoriesNode;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.NotNull;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;

public class RefreshAction extends HelmAction {

  public RefreshAction() {
    super(HelmRepositoriesNode.class);
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent event, Object selected, @NotNull Helm helm) {
    if (!(selected instanceof HelmRepositoriesNode)) {
      return;
    }
    doActionPerformed((HelmRepositoriesNode) selected, helm, getEventProject(event));
  }

  public void doActionPerformed(HelmRepositoriesNode helmRepositories, Helm helm, Project project) {
    if (helmRepositories == null) {
      return;
    }
    runWithProgress((ProgressIndicator progress) -> {
        NodeUtils.setProcessing("Refreshing...", helmRepositories);
        NodeUtils.fireModified(helmRepositories);
        NodeUtils.clearProcessing(helmRepositories);
        sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
      },
      "Refreshing...",
      project);
  }

  @Override
  protected String getTelemetryActionName() {
    return PREFIX_ACTION + "refresh helm repositories";
  }
}
