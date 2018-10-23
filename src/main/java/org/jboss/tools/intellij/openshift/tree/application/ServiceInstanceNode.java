package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.openshift.api.model.DeploymentConfig;
import me.snowdrop.servicecatalog.api.model.ServiceInstance;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;

public class ServiceInstanceNode extends KubernetesResourceMutableTreeNode {
  public ServiceInstanceNode(ServiceInstance serviceInstance) {
    super(serviceInstance);
  }

  @Override
  public void loadOnce() {
  }

}
