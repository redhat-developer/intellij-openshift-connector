package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.application.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DeleteProjectAction extends OdoAction {
  public DeleteProjectAction() {
    super(ProjectNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    ProjectNode projectNode = (ProjectNode) selected;
      CompletableFuture.runAsync(() -> {
        try {
          ExecHelper.execute(odo, "project", "delete", selected.toString(), "-f");
          ((LazyMutableTreeNode)projectNode.getParent()).remove(projectNode);
        } catch (IOException e) {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Delete project", JOptionPane.ERROR_MESSAGE));
        }
      });
  }
}
