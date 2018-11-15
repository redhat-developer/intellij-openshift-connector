package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;

public class PersistentVolumeClaimNode extends KubernetesResourceMutableTreeNode {
  public PersistentVolumeClaimNode(PersistentVolumeClaim persistentVolumeClaim) {
    super(persistentVolumeClaim);
  }

  @Override
  public String getIconName() {
    return "/images/storage.png";
  }
}
