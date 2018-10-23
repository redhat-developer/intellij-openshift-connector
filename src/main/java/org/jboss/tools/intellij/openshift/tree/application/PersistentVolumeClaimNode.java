package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import me.snowdrop.servicecatalog.api.model.ServiceInstance;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;

public class PersistentVolumeClaimNode extends KubernetesResourceMutableTreeNode {
  public PersistentVolumeClaimNode(PersistentVolumeClaim persistentVolumeClaim) {
    super(persistentVolumeClaim);
  }

  @Override
  public void loadOnce() {
  }

}
