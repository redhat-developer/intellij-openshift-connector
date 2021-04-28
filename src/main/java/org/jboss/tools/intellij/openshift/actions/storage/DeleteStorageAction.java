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
package org.jboss.tools.intellij.openshift.actions.storage;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.PersistentVolumeClaimNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DeleteStorageAction extends OdoAction {
  public DeleteStorageAction() {
    super(PersistentVolumeClaimNode.class);
  }

  @Override
  protected String getTelemetryActionName() { return "delete storage"; }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    PersistentVolumeClaimNode storageNode = (PersistentVolumeClaimNode) selected;
    ComponentNode componentNode = (ComponentNode) storageNode.getParent();
    Component component = (Component) componentNode.getComponent();
    ApplicationNode applicationNode = componentNode.getParent();
    NamespaceNode namespaceNode = applicationNode.getParent();
    if (Messages.NO == Messages.showYesNoDialog("Delete Storage '" + storageNode.getName() + "'.\nAre you sure?", "Delete Storage",
        Messages.getQuestionIcon())) {
        sendTelemetryResults(TelemetryResult.ABORTED);
        return;
    }
    CompletableFuture.runAsync(() -> {
      try {
          odo.deleteStorage(namespaceNode.getName(), applicationNode.getName(), component.getPath(), component.getName(), storageNode.getName());
          ((ApplicationsTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireRemoved(storageNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
      }
      catch (IOException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete storage"));
      }
    });
  }
}
