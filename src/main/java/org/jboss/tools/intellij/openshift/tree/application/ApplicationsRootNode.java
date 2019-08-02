/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.openapi.project.Project;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCli;
import org.jboss.tools.intellij.openshift.utils.odo.OdoProjectDecorator;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class ApplicationsRootNode extends LazyMutableTreeNode implements IconTreeNode {
  private OpenShiftClient client = loadClient();
  private boolean logged;
  private final ApplicationTreeModel model;
  private Odo odo;

  private static final String ERROR = "Please log in to the cluster";

  public ApplicationsRootNode(ApplicationTreeModel model) {
    setUserObject(client.getMasterUrl());
    this.model = model;
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

  public Odo getOdo() throws IOException {
    if (odo == null) {
        odo = new OdoProjectDecorator(OdoCli.get(), model);
    }
    return odo;
  }

  public ApplicationTreeModel getModel() {
    return model;
  }

  @Override
  public void load() {
    super.load();
    try {
      getOdo().getProjects(client).stream().forEach(p -> add(new ProjectNode(p)));
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
