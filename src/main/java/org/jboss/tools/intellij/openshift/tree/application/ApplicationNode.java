package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.OpenShiftClient;
import me.snowdrop.servicecatalog.api.client.ServiceCatalogClient;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.OdoConfig;

import javax.swing.tree.DefaultMutableTreeNode;

public class ApplicationNode extends LazyMutableTreeNode {
  public ApplicationNode(OdoConfig.Application application) {
    super(application);
  }

  @Override
  public void loadOnce() {
    OpenShiftClient client = ((ApplicationsRootNode) getRoot()).getClient();
    try {
      loadDCs(client);
    } catch (KubernetesClientException e) {
      this.add(new DefaultMutableTreeNode("Failed to load application dc or  sis"));
    }
    try {
      loadServiceInstances(client);
    } catch (KubernetesClientException e) {
    }
  }

  protected void loadDCs(OpenShiftClient client) {
    client.deploymentConfigs().inNamespace(((Project) ((ProjectNode) getParent()).getUserObject()).getMetadata().getName()).withLabelSelector(new LabelSelectorBuilder().addToMatchLabels("app", this.toString()).build()).list().getItems().forEach(dc -> this.add(new DeploymentConfigNode(dc)));
  }

  protected void loadServiceInstances(OpenShiftClient client) {
    ServiceCatalogClient sc = client.adapt(ServiceCatalogClient.class);
    sc.serviceInstances().inNamespace(((Project) ((ProjectNode) getParent()).getUserObject()).getMetadata().getName()).withLabelSelector(new LabelSelectorBuilder().addToMatchLabels("app", this.toString()).build()).list().getItems().forEach(si -> this.add(new ServiceInstanceNode(si)));
  }

  @Override
  public String toString() {
    return ((OdoConfig.Application) userObject).getName();
  }
}
