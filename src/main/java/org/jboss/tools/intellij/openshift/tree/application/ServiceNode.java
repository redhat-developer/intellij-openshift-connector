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
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;

public class ServiceNode extends KubernetesResourceMutableTreeNode {
  public ServiceNode(HasMetadata serviceResource) {
    super(serviceResource);
  }

  @Override
  public String getIconName() {
    return "/images/service.png";
  }
}
