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

public abstract class ParentableNode<T> implements ProcessingNode, StructureAwareNode {
    private final T parent;
    private final ApplicationsRootNode root;
    private final String name;
    private final ProcessingNodeImpl processingNode = new ProcessingNodeImpl();

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
        Object node = getNamespaceNode(this);
        if (node instanceof NamespaceNode) {
            return ((NamespaceNode)node).getName();
        } else {
            return "";
        }
    }

    private static Object getNamespaceNode(Object node) {
        while (!(node instanceof NamespaceNode)) {
            if (node instanceof ParentableNode) {
                node = ((ParentableNode) node).getParent();
            }
            if (node == null){
                return null;
            }
        }
        return node;
    }

    @Override
    public ApplicationsTreeStructure getStructure() {
        return getRoot().getStructure();
    }

    @Override
    public void startProcessing(String message) {
        this.processingNode.startProcessing(message);
    }

    @Override
    public void stopProcessing() {
        this.processingNode.stopProcessing();
    }

    @Override
    public boolean isProcessing() {
        return processingNode.isProcessing();
    }

    @Override
    public boolean isProcessingStopped() {
        return processingNode.isProcessingStopped();
    }

    @Override
    public String getProcessingMessage() {
        return processingNode.getMessage();
    }

}
