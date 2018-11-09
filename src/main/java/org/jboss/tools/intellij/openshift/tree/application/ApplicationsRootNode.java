package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;

import java.io.IOException;

public class ApplicationsRootNode extends LazyMutableTreeNode {
  private final OpenShiftClient client;

  public ApplicationsRootNode(OpenShiftClient client) {
    super(client.getMasterUrl());
    this.client = client;
  }

  public OpenShiftClient getClient() {
    return client;
  }

  @Override
  public void loadOnce() {
    try {
      client.projects().list().getItems().stream().forEach(p -> this.add(new ProjectNode(p)));
    } catch (KubernetesClientException e) {
      this.add(new LazyMutableTreeNode("Please login"));
    }
  }
}
