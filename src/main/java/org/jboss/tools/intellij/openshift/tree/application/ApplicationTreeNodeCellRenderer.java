package org.jboss.tools.intellij.openshift.tree.application;

import org.jboss.tools.intellij.openshift.tree.IconTreeNode;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

public class ApplicationTreeNodeCellRenderer extends DefaultTreeCellRenderer {
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component comp = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    if (value instanceof IconTreeNode) {
      String iconName = ((IconTreeNode)value).getIconName();
      if (iconName != null) {
        setIcon(new ImageIcon(ApplicationTreeNodeCellRenderer.class.getResource(iconName)));
      }
    }
    return comp;
  }
}
