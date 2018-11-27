package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;

public class ServiceNode extends KubernetesResourceMutableTreeNode {
  public ServiceNode(HasMetadata serviceResource) {
    super(serviceResource);
  }

  @Override
  public String getIconName() {
    return "/images/service.png";
  }
}
