package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.utils.odo.OdoConfig;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class CreateApplicationAction extends OdoAction {
  public CreateApplicationAction() {
    super(ProjectNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    String appName = JOptionPane.showInputDialog(null, "Appplication name", "New application", JOptionPane.QUESTION_MESSAGE);
    if ((appName != null) && appName.trim().length() > 0) {
      CompletableFuture.runAsync(() -> {
        try {
          odo.createApplication(selected.toString(), appName);
          LazyMutableTreeNode projectNode = (LazyMutableTreeNode) selected;
          OdoConfig.Application application = new OdoConfig.Application();
          application.setActive(true);
          application.setName(appName);
          application.setProject(projectNode.toString());
          projectNode.add(new ApplicationNode(application));
        } catch (IOException e) {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create application", JOptionPane.ERROR_MESSAGE));
        }
      });
    }
  }
}
