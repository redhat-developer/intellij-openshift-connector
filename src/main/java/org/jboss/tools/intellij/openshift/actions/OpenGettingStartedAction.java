/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

public class OpenGettingStartedAction extends OdoAction {
  private static final String GETTING_STARTED_WINDOW_ID = "OpenShiftGettingStarted";

  public OpenGettingStartedAction() {
    super(ApplicationsRootNode.class);
  }

  @Override
  public String getTelemetryActionName() {
    return "open getting started";
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    Project project = anActionEvent.getProject();
    if (project == null) {
      return;
    }
    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GETTING_STARTED_WINDOW_ID);
    if (toolWindow == null) {
      return;
    }
    toolWindow.setAvailable(true, null);
    toolWindow.activate(null);
    toolWindow.show(null);
  }

}
