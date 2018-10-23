package org.jboss.tools.intellij.openshift.tree;

import io.fabric8.kubernetes.api.model.NamedContext;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class ClustersTreeNodeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        value = value instanceof NamedContext ? ((NamedContext) value).getName() : value;
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value.equals(((ClustersTreeModel)tree.getModel()).getConfig().getCurrentContext())) {
            setFont(tree.getFont().deriveFont(Font.BOLD + Font.ITALIC));
            System.out.println("Setting for " + value);
        } else {
            setFont(tree.getFont());
        }
        return this;
    }
}
