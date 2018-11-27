package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.OdoHelper;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class ServiceNode extends KubernetesResourceMutableTreeNode {
  public ServiceNode(HasMetadata serviceResource) {
    super(serviceResource);
  }

  @Override
  public String getIconName() {
    return "/images/service.png";
  }
}
