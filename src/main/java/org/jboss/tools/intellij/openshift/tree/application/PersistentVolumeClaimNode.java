package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;

public class PersistentVolumeClaimNode extends KubernetesResourceMutableTreeNode {
  public PersistentVolumeClaimNode(PersistentVolumeClaim persistentVolumeClaim) {
    super(persistentVolumeClaim);
  }

  @Override
  public String getIconName() {
    return "/images/storage.png";
  }

  private String getStorageName() {
    HasMetadata data = (HasMetadata) getUserObject();
    String res = null;
    if (data.getMetadata().getLabels() != null) {
      res = data.getMetadata().getLabels().get(KubernetesLabels.STORAGE_NAME_LABEL);
    }
    return res;
  }

  @Override
  public String toString() {
    String res = getStorageName();
    if (res == null) {
      res =  super.toString();
    }
    return res;
  }
}
