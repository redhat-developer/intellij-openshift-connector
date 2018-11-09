package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.tree.BaseTreeModel;
import com.intellij.util.ui.tree.AbstractTreeModel;
import org.jboss.tools.intellij.openshift.tree.RefreshableTreeModel;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.OdoConfig;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
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
    String appName = JOptionPane.showInputDialog(null, "Appplication name", "New application", JOptionPane.QUESTION_MESSAGE);
    if ((appName != null) && appName.trim().length() > 0) {
      CompletableFuture.runAsync(() -> {
        try {
          ExecHelper.execute(odo, "project", "set", selected.toString());
          ExecHelper.execute(odo, "app", "create", appName);
          ProjectNode projectNode = (ProjectNode) selected;
          OdoConfig.Application application = new OdoConfig.Application();
          application.setActive(true);
          application.setName(appName);
          application.setProject(projectNode.toString());
          projectNode.add(new ApplicationNode(application));
          ((ApplicationTreeModel) getTree(anActionEvent).getModel()).treeNodesInserted(path, new int[]{projectNode.getChildCount() - 1}, new Object[0]);
        } catch (IOException e) {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create application", JOptionPane.ERROR_MESSAGE));
        }
      });
    }
  }
}
