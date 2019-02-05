/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.jboss.tools.intellij.openshift.KubernetesLabels;

public class KubernetesResourceMutableTreeNode extends LazyMutableTreeNode implements IconTreeNode {
  public KubernetesResourceMutableTreeNode() {
  }

  public KubernetesResourceMutableTreeNode(HasMetadata userObject) {
    super(userObject);
  }

  public KubernetesResourceMutableTreeNode(HasMetadata userObject, boolean allowsChildren) {
    super(userObject, allowsChildren);
  }

  @Override
  public String toString() {
    return KubernetesLabels.getComponentName((HasMetadata) userObject);
  }

  @Override
  public String getIconName() {
    return null;
  }
}
