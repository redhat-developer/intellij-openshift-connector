package org.jboss.tools.intellij.openshift.tree;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.jboss.tools.intellij.openshift.KubernetesLabels;

public class KubernetesResourceMutableTreeNode extends LazyMutableTreeNode implements IconTreeNode {
  public KubernetesResourceMutableTreeNode() {
  }

  public KubernetesResourceMutableTreeNode(HasMetadata userObject) {
    super(userObject);
  }

  public KubernetesResourceMutableTreeNode(HasMetadata userObject, boolean allowsChildren) {
    super(userObject, allowsChildren);
  }

  @Override
  public String toString() {
    final ObjectMeta metadata = ((HasMetadata)userObject).getMetadata();
    if (metadata.getLabels() != null) {
      return metadata.getLabels().computeIfAbsent(KubernetesLabels.COMPONENT_NAME_LABEL,
        key -> ((HasMetadata)userObject).getMetadata().getName());
    } else {
      return metadata.getName();
    }
  }

  @Override
  public String getIconName() {
    return null;
  }
}
