package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DeleteApplicationAction extends OdoAction {
  public DeleteApplicationAction() {
    super(ApplicationNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    ApplicationNode applicationNode = (ApplicationNode) selected;
    ProjectNode projectNode = (ProjectNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        Runtime.getRuntime().exec(new String[]{odo, "project", "set", projectNode.toString()});
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    }).thenRun(() -> {
      try {
        Runtime.getRuntime().exec(new String[]{odo, "app", "delete", "-f", applicationNode.toString()});
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    }).
      thenRun(() -> {
        int index = projectNode.getIndex(applicationNode);
        projectNode.remove(index);
        ((ApplicationTreeModel) getTree(anActionEvent).getModel()).treeNodesRemoved(path.getParentPath(), new int[]{index}, new Object[0]);
      })
      .exceptionally((t) -> {
        JOptionPane.showMessageDialog(null, "Error: " + t.getLocalizedMessage(), "Delete application", JOptionPane.ERROR_MESSAGE);
        return null;
      });
  }
}

