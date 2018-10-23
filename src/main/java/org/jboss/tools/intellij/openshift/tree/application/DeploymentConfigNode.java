package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.OdoConfig;

import javax.swing.tree.DefaultMutableTreeNode;

public class DeploymentConfigNode extends KubernetesResourceMutableTreeNode {
  public DeploymentConfigNode(DeploymentConfig deploymentConfig) {
    super(deploymentConfig);
  }

  @Override
  public void loadOnce() {
    try {
      OpenShiftClient client = ((ApplicationsRootNode) getRoot()).getClient();
      client.persistentVolumeClaims().inNamespace(((Project) ((ProjectNode) getParent().getParent()).getUserObject()).getMetadata().getName()).withLabelSelector(new LabelSelectorBuilder().addToMatchLabels("app", this.getParent().toString()).build()).list().getItems().forEach(pvc -> this.add(new PersistentVolumeClaimNode(pvc)));
    } catch (KubernetesClientException e) {
      this.add(new DefaultMutableTreeNode("Failed to load persistent volume claims"));
    }
  }

}
