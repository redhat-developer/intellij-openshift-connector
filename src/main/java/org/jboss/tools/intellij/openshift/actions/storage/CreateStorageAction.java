/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
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
import org.jboss.tools.intellij.openshift.actions.component.ContextAwareComponentAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.ui.storage.CreateStorageDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateStorageAction extends ContextAwareComponentAction {

  @Override
  protected String getTelemetryActionName() { return "create storage"; }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = componentNode.getComponent();
    ApplicationNode applicationNode = componentNode.getParent();
    NamespaceNode namespaceNode = applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        CreateStorageDialog dialog = UIHelper.executeInUI(this::showDialog);
        if (dialog.isOK()) {
          odo.createStorage(namespaceNode.getName(), applicationNode.getName(), component.getPath(), component.getName(), dialog.getName(), dialog.getMountPath(), dialog.getStorageSize());
          ((ApplicationsTreeStructure)getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(componentNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
        } else {
          sendTelemetryResults(TelemetryResult.ABORTED);
        }
      } catch (IOException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create storage"));
      }
    });
  }

  private CreateStorageDialog showDialog() {
    CreateStorageDialog dialog = new CreateStorageDialog(null);
    dialog.show();
    return dialog;
  }
}
