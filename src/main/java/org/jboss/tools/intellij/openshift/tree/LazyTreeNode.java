package org.jboss.tools.intellij.openshift.tree;

import javax.swing.tree.TreeNode;

public interface LazyTreeNode extends TreeNode {
    public boolean load();
    public void reload();
}
