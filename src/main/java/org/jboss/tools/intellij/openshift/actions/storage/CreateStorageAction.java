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
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.ui.storage.CreateStorageDialog;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateStorageAction extends OdoAction {
  public CreateStorageAction() {
    super(ComponentNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        CreateStorageDialog dialog = UIHelper.executeInUI(this::showDialog);
        if (dialog.isOK()) {
          odo.createStorage(projectNode.toString(), applicationNode.toString(), componentNode.toString(), dialog.getName(), dialog.getMountPath(), dialog.getStorageSize());
          componentNode.reload();
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create storage", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  private CreateStorageDialog showDialog() {
    CreateStorageDialog dialog = new CreateStorageDialog(null);
    dialog.show();
    return dialog;
  }

  public List<Integer> loadServicePorts(Odo odo, LazyMutableTreeNode projectNode, LazyMutableTreeNode applicationNode, LazyMutableTreeNode componentNode) {
    final OpenShiftClient client = ((ApplicationsRootNode)componentNode.getRoot()).getClient();
    return odo.getServicePorts(client, projectNode.toString(), applicationNode.toString(), componentNode.toString());
  }
}
