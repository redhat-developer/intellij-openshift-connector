package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.tree.BaseTreeModel;
import com.intellij.util.ui.tree.AbstractTreeModel;
import org.jboss.tools.intellij.openshift.tree.RefreshableTreeModel;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.utils.OdoConfig;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class CreateApplicationAction extends OdoAction {
  public CreateApplicationAction() {
    super(ProjectNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    String appName = JOptionPane.showInputDialog("Appplication name");
    if ((appName != null) && appName.trim().length() > 0) {
      CompletableFuture.runAsync(() -> {
        try {
          Runtime.getRuntime().exec(new String[] {odo, "project", "set", selected.toString()});
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      }).thenRun(() -> {
        try {
          Runtime.getRuntime().exec(new String[] {odo, "app", "create", appName});
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      }).
        thenRun(() -> {
          ProjectNode projectNode = (ProjectNode) selected;
          OdoConfig.Application application = new OdoConfig.Application();
          application.setActive(true);
          application.setName(appName);
          application.setProject(projectNode.toString());
          projectNode.add(new ApplicationNode(application));
          ((ApplicationTreeModel)getTree(anActionEvent).getModel()).treeNodesInserted(path, new int[] {projectNode.getChildCount() -1}, new Object[0]);
        })
        .exceptionally((t) -> {
          JOptionPane.showMessageDialog(null, "Error: " + t.getLocalizedMessage(), "Create application", JOptionPane.ERROR_MESSAGE);
          return null;
        });
    }
  }
}
