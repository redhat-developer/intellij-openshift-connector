package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.utils.OdoConfig;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.util.Map;

public class ProjectNode extends KubernetesResourceMutableTreeNode {
  public ProjectNode(Project project) {
    super(project);
  }

  @Override
  public void loadOnce() {
    try {
      System.out.println("Loading " + this);
      ConfigHelper.loadOdoConfig().getActiveApplications().stream().filter(a -> a.getProject().equals(((Project) userObject).getMetadata().getName())).forEach(a -> this.add(new ApplicationNode(a)));
    } catch (IOException e) {
      this.add(new DefaultMutableTreeNode("Failed to load Odo config file"));
    }
  }
}
