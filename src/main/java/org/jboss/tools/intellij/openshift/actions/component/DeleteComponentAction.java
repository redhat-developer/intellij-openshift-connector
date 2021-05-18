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
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DeleteComponentAction extends OdoAction {
  public DeleteComponentAction() {
    super(ComponentNode.class);
  }

  @Override
  protected String getTelemetryActionName() { return "delete component"; }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Odo odo, Object selected) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = componentNode.getComponent();
    ApplicationNode applicationNode = componentNode.getParent();
    NamespaceNode namespaceNode = applicationNode.getParent();
    if (Messages.NO == Messages.showYesNoDialog("Delete Component '" + component.getName() + "'.\nAre you sure?", "Delete Component",
      Messages.getQuestionIcon())) {
      sendTelemetryResults(TelemetryResult.ABORTED);
      return;
    }

    CompletableFuture.runAsync(() -> {
      try {
        odo.deleteComponent(namespaceNode.getName(), applicationNode.getName(), component.getPath(), component.getName(), component.getInfo()!= null ? component.getInfo().getComponentKind(): null);
        ((ApplicationsTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireRemoved(componentNode);
        sendTelemetryResults(TelemetryResult.SUCCESS);
      } catch (IOException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete component"));
      }
    });
  }
}
