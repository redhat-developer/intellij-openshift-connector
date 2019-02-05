/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree;

import io.fabric8.kubernetes.api.model.NamedContext;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.awt.Font;

public class ClustersTreeNodeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        value = value instanceof NamedContext ? ((NamedContext) value).getName() : value;
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value.equals(((ClustersTreeModel)tree.getModel()).getConfig().getCurrentContext())) {
            setFont(tree.getFont().deriveFont(Font.BOLD + Font.ITALIC));
        } else {
            setFont(tree.getFont());
        }
        return this;
    }
}
