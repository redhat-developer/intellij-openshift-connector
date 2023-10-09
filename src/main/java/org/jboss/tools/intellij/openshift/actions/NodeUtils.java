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

import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.ProcessingNode;
import org.jboss.tools.intellij.openshift.tree.application.StructureAwareNode;

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

    public static void clearProcessing(ProcessingNode node) {
        if (node == null) {
            return;
        }
        node.stopProcessing();
        NodeUtils.fireModified(node);
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
}
