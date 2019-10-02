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
package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DeleteApplicationAction extends OdoAction {
  public DeleteApplicationAction() {
    super(ApplicationNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) selected;
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    OpenShiftClient client = ((ApplicationsRootNode)applicationNode.getRoot()).getClient();
    CompletableFuture.runAsync(() -> {
      try {
        odo.deleteApplication(client, projectNode.toString(), applicationNode.toString());
        projectNode.remove(applicationNode);
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Delete application", JOptionPane.ERROR_MESSAGE));
      }
    });
  }
}

