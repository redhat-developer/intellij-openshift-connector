/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCliFactory;
import org.jboss.tools.intellij.openshift.utils.odo.OdoProjectDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;

public class ApplicationsRootNode extends LazyMutableTreeNode implements IconTreeNode {
  private boolean logged;
  private final ApplicationTreeModel model;
  private Odo odo;

  private static final String ERROR = "Please log in to the cluster";

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationsRootNode.class);

  public ApplicationsRootNode(ApplicationTreeModel model) {
    odo = new OdoProjectDecorator(OdoCliFactory.getInstance().getOdo(model.getProject()), model);
    setUserObject(odo.getMasterUrl());
    this.model = model;
  }

  public boolean isLogged() {
    return logged;
  }

  public void setLogged(boolean logged) {
    this.logged = logged;
  }

  public Odo getOdo(){
    return odo;
  }

  public ApplicationTreeModel getModel() {
    return model;
  }

  @Override
  public void load() {
    super.load();
    try {
      odo.getProjects().forEach(p -> add(new ProjectNode(p)));
      setLogged(true);
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      add(new DefaultMutableTreeNode(ERROR));
    }
  }

  @Override
  public void reload() {
    odo = OdoCliFactory.getInstance().getOdo(model.getProject());
    super.reload();
  }

  @Override
  public String getIconName() {
    return "/images/cluster.png";
  }
}
