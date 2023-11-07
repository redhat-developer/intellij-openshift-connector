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

import org.jboss.tools.intellij.openshift.utils.odo.Starter;

public class DevfileRegistryComponentTypeStarterNode extends BaseNode<DevfileRegistryComponentTypeNode> {
  private final Starter starter;

  public DevfileRegistryComponentTypeStarterNode(ApplicationsRootNode root, DevfileRegistryComponentTypeNode parent, Starter starter) {
    super(root, parent, starter.getName());
    this.starter = starter;
  }

  public Starter getStarter() {
    return starter;
  }
}
