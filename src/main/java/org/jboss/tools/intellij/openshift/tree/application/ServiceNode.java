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

public class ServiceNode extends ParentableNode<ApplicationNode> {
public ServiceNode(ApplicationNode parent, HasMetadata serviceResource) {
    super(parent.getRoot(), parent, serviceResource.getMetadata().getName());
  }
}
