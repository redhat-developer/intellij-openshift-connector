package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;

public class ComponentNode extends KubernetesResourceMutableTreeNode {
  public ComponentNode(HasMetadata componentResource) {
    super(componentResource);
  }

  @Override
  public String getIconName() {
    return "/images/component.png";
  }
}
