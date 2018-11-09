package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.application.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class CreateProjectAction extends OdoAction {
  public CreateProjectAction() {
    super(ApplicationsRootNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    String projectName = JOptionPane.showInputDialog(null, "Project name", "New project", JOptionPane.QUESTION_MESSAGE);
    if ((projectName != null) && projectName.trim().length() > 0) {
      CompletableFuture.runAsync(() -> {
        try {
          ExecHelper.execute(odo, "project", "create", projectName);
        } catch (IOException e) {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create project", JOptionPane.ERROR_MESSAGE));
        }
      });
    }
  }
}
