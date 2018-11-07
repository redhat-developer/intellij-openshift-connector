package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.application.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.*;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DeleteComponentAction extends OdoAction {
  public DeleteComponentAction() {
    super(ComponentNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    TreeNode serviceNode = (TreeNode) selected;
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    ProjectNode projectNode = (ProjectNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        ExecHelper.execute(odo, "project", "set", projectNode.toString());
        ExecHelper.execute(odo, "app", "set", applicationNode.toString());
        ExecHelper.execute(odo, "delete", serviceNode.toString(), "-f");
        applicationNode.reload();
        ((ApplicationTreeModel)getTree(anActionEvent).getModel()).treeStructureChanged(path.getParentPath(), new int[0], new Object[0]);
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Delete component", JOptionPane.ERROR_MESSAGE));
      }
    });
  }
}
