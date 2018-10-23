package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.NamedContext;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class ApplicationTreeNodeCellRenderer implements TreeCellRenderer {
    private final TreeCellRenderer parent;

    public ApplicationTreeNodeCellRenderer(TreeCellRenderer parent) {
        this.parent = parent;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        value = value instanceof HasMetadata ? ((HasMetadata) value).getMetadata().getName() : value;
        return parent.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}
