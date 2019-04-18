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
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.*;
import org.jboss.tools.intellij.openshift.ui.url.CreateURLDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateURLAction extends OdoAction {
  public CreateURLAction() {
    super(ComponentNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        createURL(odo, projectNode, applicationNode, componentNode);
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create URL", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  public static List<Integer> loadServicePorts(Odo odo, LazyMutableTreeNode projectNode, LazyMutableTreeNode applicationNode, LazyMutableTreeNode componentNode) {
    final OpenShiftClient client = ((ApplicationsRootNode)componentNode.getRoot()).getClient();
    return odo.getServicePorts(client, projectNode.toString(), applicationNode.toString(), componentNode.toString());
  }

  public static boolean createURL(Odo odo, LazyMutableTreeNode projectNode, LazyMutableTreeNode applicationNode, ComponentNode componentNode) throws IOException {
    boolean done = false;
    List<Integer> ports = loadServicePorts(odo, projectNode, applicationNode, componentNode);
    if (!ports.isEmpty()) {
      CreateURLDialog dialog = UIHelper.executeInUI(() -> {
        CreateURLDialog dialog1 = new CreateURLDialog(null);
        dialog1.setPorts(ports);
        dialog1.show();
        return dialog1;
      });
      if (dialog.isOK()) {
        Integer port = dialog.getSelectedPort();
        String name = dialog.getName();
        if (port != null) {
          odo.createURL(projectNode.toString(), applicationNode.toString(), componentNode.toString(), name, port);
          componentNode.reload();
          done = true;
        }
      }
    } else {
      throw new IOException("Can't create url for component without ports");
    }
    return done;
  }
}
