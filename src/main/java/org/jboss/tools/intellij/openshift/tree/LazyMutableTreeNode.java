package org.jboss.tools.intellij.openshift.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyMutableTreeNode extends DefaultMutableTreeNode implements LazyTreeNode {
    private AtomicBoolean needsLoading = new AtomicBoolean(true);

    public LazyMutableTreeNode() {
    }

    public LazyMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public LazyMutableTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    public boolean load() {
        if (needsLoading.getAndSet(false)) {
            loadOnce();
            return true;
        }
        return false;
    }

    public void loadOnce() {
    }
}
