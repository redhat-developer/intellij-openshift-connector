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

import org.jboss.tools.intellij.openshift.utils.odo.DevfileRegistry;

public class DevfileRegistryNode extends ParentableNode<DevfileRegistriesNode> {
  private final DevfileRegistry registry;

  public DevfileRegistryNode(ApplicationsRootNode root, DevfileRegistriesNode parent, DevfileRegistry registry) {
    super(root, parent, registry.getName());
    this.registry = registry;
  }

  public DevfileRegistry getRegistry() {
    return registry;
  }
}
