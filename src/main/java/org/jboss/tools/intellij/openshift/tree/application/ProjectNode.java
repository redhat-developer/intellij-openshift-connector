package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.openshift.api.model.Project;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;

public class ProjectNode extends KubernetesResourceMutableTreeNode {
  public ProjectNode(Project project) {
    super(project);
  }

  @Override
  public String getIconName() {
    return "/images/project.png";
  }
}
