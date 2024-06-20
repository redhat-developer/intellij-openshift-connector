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
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ApplicationUtils;
import com.redhat.devtools.intellij.common.utils.SwingUtils;
import java.awt.Point;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.jboss.tools.intellij.openshift.actions.HelmAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoriesNode;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoryNode;
import org.jboss.tools.intellij.openshift.ui.helm.EditHelmRepoDialog;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.helm.HelmRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EditHelmRepoAction extends HelmAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(EditHelmRepoAction.class);

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Helm helm) {
    Project project = getEventProject(anActionEvent);
    if (!(selected instanceof HelmRepositoryNode repositoryNode)) {
      return;
    }
    openAddHelmRepoDialog(repositoryNode, repositoryNode.getParent(), helm, project, SwingUtils.getMouseLocation(anActionEvent));
  }

  private void openAddHelmRepoDialog(HelmRepositoryNode repositoryNode, HelmRepositoriesNode repositoriesNode, Helm helm, Project project, Point location) {
    CompletableFuture.supplyAsync(
      () -> listRepositories(helm),
      ApplicationUtils.PLATFORM_EXECUTOR
    ).thenAcceptAsync(
      repositories -> {
        EditHelmRepoDialog dialog = new EditHelmRepoDialog(repositories, repositoryNode, repositoriesNode, helm, project, location);
        sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
        dialog.show();
      },
      ApplicationUtils.UI_EXECUTOR
    );
  }

  private Collection<HelmRepository> listRepositories(Helm helm) {
    try {
      return helm.listRepos();
    } catch (IOException e) {
      LOGGER.warn("Could not list helm repositories", e);
      return Collections.emptyList();
    }
  }

  @Override
  public String getTelemetryActionName() {
    return "helm-edit repo";
  }

  @Override
  public boolean isVisible(Object selected) {
    return selected instanceof HelmRepositoryNode;
  }
}
