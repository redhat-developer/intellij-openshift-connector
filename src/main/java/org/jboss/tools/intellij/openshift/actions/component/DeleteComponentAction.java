package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.application.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.*;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class DeleteComponentAction extends OdoAction {
  public DeleteComponentAction() {
    super(DeploymentConfigNode.class, ServiceInstanceNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    TreeNode serviceNode = (TreeNode) selected;
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    ProjectNode projectNode = (ProjectNode) applicationNode.getParent();
    try {
      ExecHelper.execute(odo, "project", "set", projectNode.toString())
        .thenCompose(s -> ExecHelper.execute(odo, "app", "set", applicationNode.toString()))
        .thenCompose(s -> ExecHelper.execute(odo, "delete", serviceNode.toString(), "-f"))
        .thenAccept(s -> applicationNode.reload())
        .thenAccept(v -> ((ApplicationTreeModel)getTree(anActionEvent).getModel()).treeStructureChanged(path.getParentPath(), new int[0], new Object[0]))
        .exceptionally(t -> {
          JOptionPane.showMessageDialog(null, "Error: " + t.getLocalizedMessage(), "Delete component", JOptionPane.ERROR_MESSAGE);
          return null;

        });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
