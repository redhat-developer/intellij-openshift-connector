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

import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;

public class ComponentNode extends LazyMutableTreeNode implements IconTreeNode {
  public ComponentNode(Component component) {
    super(component);
  }

  @Override
  public void load() {
    super.load();
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) getRoot();
    Component component = (Component) getUserObject();
      try {
          Odo odo = clusterNode.getOdo();
          try {
            odo.getStorages(getParent().getParent().toString(), getParent().toString(), component.getPath(), component.getName()).forEach(storage -> add(new PersistentVolumeClaimNode(storage)));
          } catch (KubernetesClientException e) {}
          try {
            odo.listURLs(getParent().getParent().toString(), getParent().toString(), component.getPath(), component.getName()).forEach(url -> add(new URLNode(url)));
          } catch (IOException e) {}
      } catch (IOException e) {}
  }

    @Override
    public String toString() {
      Component component = (Component) getUserObject();
        return component.getName() + ' ' + component.getState();
    }

    @Override
  public String getIconName() {
    return "/images/component.png";
  }
}
