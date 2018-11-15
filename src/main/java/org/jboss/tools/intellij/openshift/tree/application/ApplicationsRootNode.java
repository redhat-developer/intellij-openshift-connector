package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;

public class ApplicationsRootNode extends LazyMutableTreeNode {
  private final OpenShiftClient client;

  public ApplicationsRootNode(OpenShiftClient client) {
    super(client.getMasterUrl());
    this.client = client;
  }

  public OpenShiftClient getClient() {
    return client;
  }
}
