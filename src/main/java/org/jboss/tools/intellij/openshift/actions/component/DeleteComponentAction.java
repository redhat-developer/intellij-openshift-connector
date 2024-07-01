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
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DeleteComponentAction extends OdoAction {
  public DeleteComponentAction() {
    super(ComponentNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "delete component";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = componentNode.getComponent();
    NamespaceNode namespaceNode = componentNode.getParent();

    if (Messages.NO == Messages.showYesNoDialog("Delete Component '" + component.getName() + "'.\nAre you sure?", "Delete Component",
      Messages.getQuestionIcon())) {
      sendTelemetryResults(TelemetryResult.ABORTED);
      return;
    }

    runWithProgress((ProgressIndicator progress) -> {
        setProcessing("Deleted", componentNode);
        try {
          odo.deleteComponent(
            namespaceNode.getName(),
            component.getPath(),
            component.getName(),
            component.getInfo().getComponentKind());
          componentNode.stopProcessing();
          componentNode.getRoot().getStructure().fireRemoved(componentNode);
          clearProcessing(componentNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
        } catch (IOException e) {
          clearProcessing(componentNode);
          sendTelemetryError(e);
          UIHelper.executeInUI(() ->
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete Component"));
        }
      },
      "Delete Component...",
      getEventProject(anActionEvent));
  }
}
