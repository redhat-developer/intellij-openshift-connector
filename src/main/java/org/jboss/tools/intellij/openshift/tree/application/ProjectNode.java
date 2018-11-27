package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.openshift.api.model.Project;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class ProjectNode extends KubernetesResourceMutableTreeNode {
  public ProjectNode(Project project) {
    super(project);
  }

  @Override
  public void load() {
    super.load();
    try {
      Odo.get().getApplication(toString()).forEach(a -> add(new ApplicationNode(a)));
    } catch (IOException e) {
      add(new DefaultMutableTreeNode("Failed to load applications"));
    }
  }

  @Override
  public String getIconName() {
    return "/images/project.png";
  }
}
