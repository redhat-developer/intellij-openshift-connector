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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class ComponentNode extends KubernetesResourceMutableTreeNode {
  public ComponentNode(HasMetadata componentResource) {
    super(componentResource);
  }

  @Override
  public void load() {
    super.load();
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) getRoot();
    try {
      Odo.get().getStorages(clusterNode.getClient(), getParent().getParent().toString(), getParent().toString(), toString()).forEach(pvc -> add(new PersistentVolumeClaimNode(pvc)));
    } catch (KubernetesClientException|IOException e) {}
    try {
      Odo.get().listURLs(getParent().getParent().toString(), getParent().toString(), toString()).forEach(url -> add(new URLNode(url)));
    } catch (IOException e) {}
  }

  @Override
  public String getIconName() {
    return "/images/component.png";
  }
}
