/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import org.jboss.tools.intellij.openshift.utils.odo.Binding;

public class BindingNode extends BaseNode<ComponentNode> {
  private final Binding binding;

  public BindingNode(ComponentNode parent, Binding binding) {
    super(parent.getRoot(), parent, binding.getName());
    this.binding = binding;
  }

  public Binding getBinding() {
    return binding;
  }
}
