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
import org.jboss.tools.intellij.openshift.actions.project.CreateProjectAction;

public class CreateNamespaceLinkNode extends MessageNode<ApplicationsRootNode> implements LinkElement {
    protected CreateNamespaceLinkNode(ApplicationsRootNode root) {
        super(root, root, "<a>No namespaces/projects, click here to create one.</a>");
    }

    @Override
    public void execute() {
        CreateProjectAction.execute(getParent());
    }
}
