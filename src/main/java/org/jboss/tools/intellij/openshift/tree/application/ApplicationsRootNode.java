package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.DefaultMutableTreeNode;

public class ApplicationsRootNode extends LazyMutableTreeNode implements IconTreeNode {
  private OpenShiftClient client = loadClient();
  private boolean logged;

  private static final String ERROR = "Please log in to the cluster";

  public ApplicationsRootNode() {
    setUserObject(client.getMasterUrl());
  }

  public OpenShiftClient getClient() {
    return client;
  }

  private OpenShiftClient loadClient() {
    return new DefaultOpenShiftClient(new ConfigBuilder().build());
  }

  public boolean isLogged() {
    return logged;
  }

  public void setLogged(boolean logged) {
    this.logged = logged;
  }

  @Override
  public void load() {
    super.load();
    try {
      Odo.get().getProjects(client).stream().forEach(p -> add(new ProjectNode(p)));
      setLogged(true);
    } catch (Exception e) {
      add(new DefaultMutableTreeNode(ERROR));
    }
  }

  @Override
  public void reload() {
    client = loadClient();
    super.reload();
  }

  @Override
  public String getIconName() {
    return "/images/cluster.png";
  }
}
