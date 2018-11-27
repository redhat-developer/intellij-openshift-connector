package org.jboss.tools.intellij.openshift.actions.service;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DeleteServiceAction extends OdoAction {
  public DeleteServiceAction() {
    super(ServiceNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ServiceNode serviceNode = (ServiceNode) selected;
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        odo.deleteService(projectNode.toString(), applicationNode.toString(), serviceNode.toString());
        applicationNode.remove(serviceNode);
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Delete service", JOptionPane.ERROR_MESSAGE));
      }
    });
  }
}
