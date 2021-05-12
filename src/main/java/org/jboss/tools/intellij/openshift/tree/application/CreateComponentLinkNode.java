/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import com.redhat.devtools.intellij.common.tree.LinkElement;
import org.jboss.tools.intellij.openshift.actions.component.CreateComponentAction;

public class CreateComponentLinkNode extends MessageNode<ParentableNode<? extends Object>> implements LinkElement {
    protected CreateComponentLinkNode(ApplicationsRootNode root, ParentableNode<? extends Object> parent) {
        super(root, parent, "<a>No deployments, click here to create one.</a>");
    }

    @Override
    public void execute() {
        CreateComponentAction.execute(getParent());
    }
}
