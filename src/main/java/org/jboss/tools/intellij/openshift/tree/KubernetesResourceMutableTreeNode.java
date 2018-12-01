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
    return KubernetesLabels.getComponentName((HasMetadata) userObject);
  }

  @Override
  public String getIconName() {
    return null;
  }
}
