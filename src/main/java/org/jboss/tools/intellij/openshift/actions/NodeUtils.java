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

public class NodeUtils {

    private NodeUtils() {
    }

    public static boolean hasContext(Object node) {
        return node instanceof ComponentNode
                && ((ComponentNode) node).getComponent() != null
                && ((ComponentNode) node).getComponent().hasContext();
    }
}
