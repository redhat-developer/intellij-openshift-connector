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

public abstract class ParentableNode<T> {
    private final T parent;
    private final ApplicationsRootNode root;
    private final String name;

    protected ParentableNode(ApplicationsRootNode root, T parent, String name) {
        this.root = root;
        this.parent = parent;
        this.name = name;
    }

    public ApplicationsRootNode getRoot() {
        return root;
    }

    public T getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        Object element = this;
        while (!(element instanceof NamespaceNode)) {
            if (element instanceof ParentableNode) {
                element = ((ParentableNode) element).getParent();
            }
            if (element == null){
                return "";
            }
        }
        return ((NamespaceNode)element).getName();
    }

}
