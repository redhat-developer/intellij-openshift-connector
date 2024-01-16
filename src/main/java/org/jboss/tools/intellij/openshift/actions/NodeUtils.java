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
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.ide.util.treeView.NodeDescriptor;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;
import org.jboss.tools.intellij.openshift.tree.application.ProcessingNode;
import org.jboss.tools.intellij.openshift.tree.application.StructureAwareNode;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Arrays;

public class NodeUtils {

    private NodeUtils() {
    }

    public static void setProcessing(String message, ProcessingNode node) {
        if (node == null) {
            return;
        }
        node.startProcessing(message);
        NodeUtils.fireModified(node);
    }

    public static void setProcessing(String message,  StructureAwareNode parent, ProcessingNode... nodes) {
        if (nodes == null
          || nodes.length == 0) {
            return;
        }
        Arrays.stream(nodes).forEach(node -> node.startProcessing(message));
        NodeUtils.fireModified(parent);
    }

    public static void clearProcessing(ProcessingNode node) {
        if (node == null) {
            return;
        }
        node.stopProcessing();
        NodeUtils.fireModified(node);
    }

    public static void clearProcessing(StructureAwareNode parent, ProcessingNode... nodes) {
        if (nodes == null
          || nodes.length == 0) {
            return;
        }
        Arrays.stream(nodes).forEach(ProcessingNode::stopProcessing);
        NodeUtils.fireModified(parent);
    }

    public static boolean hasContext(Object node) {
        return node instanceof ComponentNode
                && ((ComponentNode) node).getComponent() != null
                && ((ComponentNode) node).getComponent().hasContext();
    }

    public static void fireModified(Object node) {
        if (!(node instanceof StructureAwareNode)) {
            return;
        }
        fireModified((StructureAwareNode) node);
    }

    public static void fireModified(StructureAwareNode node) {
        node.getStructure().fireModified(node);
    }

    public static void fireRemoved(StructureAwareNode node) {
        node.getStructure().fireRemoved(node);
    }

    public static ApplicationsRootNode getRoot(Object selected) {
        ProcessingNode node = getElement(selected);
        if (!(node instanceof ParentableNode<?>)) {
            return null;
        }
        return ((ParentableNode<?>) node).getRoot();
    }

    public static <T> T getElement(Object selected) {
        if (selected instanceof DefaultMutableTreeNode) {
            selected = ((DefaultMutableTreeNode)selected).getUserObject();
        }
        if (selected instanceof NodeDescriptor) {
            selected = ((NodeDescriptor)selected).getElement();
        }
        return (T) selected;
    }

}
