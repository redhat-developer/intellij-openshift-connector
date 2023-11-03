/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import org.jboss.tools.intellij.openshift.utils.helm.ChartRelease;

public class ChartReleaseNode extends ParentableNode<NamespaceNode> {
  private final ChartRelease release;

  public ChartReleaseNode(NamespaceNode parent, ChartRelease release) {
    super(parent.getRoot(), parent, release.getName());
    this.release = release;
  }

  public ChartRelease getChartRelease() {
    return release;
  }
}
