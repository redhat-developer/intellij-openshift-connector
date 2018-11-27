package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.OdoConfig;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;


public class ApplicationNode extends LazyMutableTreeNode implements IconTreeNode {
  public ApplicationNode(OdoConfig.Application application) {
    super(application);
  }

  @Override
  public void load() {
    super.load();
    try {
      Odo odo = Odo.get();
      try {
        odo.getComponents(((ApplicationsRootNode)getParent().getParent()).getClient(), getParent().toString(), toString()).forEach(dc -> add(new ComponentNode(dc)));
      } catch (KubernetesClientException e) {
        add(new DefaultMutableTreeNode("Failed to load application deployment configs"));
      }
      try {
        odo.getServices(((ApplicationsRootNode)getParent().getParent()).getClient(), getParent().toString(), toString()).forEach(si -> add(new ServiceNode(si)));
      } catch (KubernetesClientException e) {}
    } catch (IOException e) {
      add(new DefaultMutableTreeNode("Failed to load application"));
    }
  }

  @Override
  public String toString() {
    return ((OdoConfig.Application) userObject).getName();
  }

  @Override
  public String getIconName() {
    return "/images/application.png";
  }
}
