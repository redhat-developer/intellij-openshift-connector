package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectBuilder;
import org.jboss.tools.intellij.openshift.actions.cluster.LoggedInClusterAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class CreateProjectAction extends LoggedInClusterAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    LazyMutableTreeNode clusterNode = (LazyMutableTreeNode) selected;
    String projectName = JOptionPane.showInputDialog(null, "Project name", "New project", JOptionPane.QUESTION_MESSAGE);
    if ((projectName != null) && projectName.trim().length() > 0) {
      CompletableFuture.runAsync(() -> {
        try {
          odo.createProject(projectName);
          Project project = new ProjectBuilder().editOrNewMetadata().withName(projectName).endMetadata().build();
          clusterNode.add(new ProjectNode(project));
        } catch (IOException e) {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create project", JOptionPane.ERROR_MESSAGE));
        }
      });
    }
  }
}
