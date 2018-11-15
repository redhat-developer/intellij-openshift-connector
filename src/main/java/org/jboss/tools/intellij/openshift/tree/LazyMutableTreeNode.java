package org.jboss.tools.intellij.openshift.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class LazyMutableTreeNode extends DefaultMutableTreeNode  {
    public static interface ChangeListener {
        void onChildAdded(LazyMutableTreeNode source, Object child, int index);
        void onChildRemoved(LazyMutableTreeNode source, Object child, int index);
        void onChildrensRemoved(LazyMutableTreeNode source);
    }

    private boolean loaded = false;
    private final List<ChangeListener> listeners = new ArrayList<>();

    public LazyMutableTreeNode() {
    }

    public LazyMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public LazyMutableTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void reload() {
        setLoaded(false);
        removeAllChildren();
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    protected void notifyChildAdded(Object child, int index) {
        for(ChangeListener listener : listeners) {
            listener.onChildAdded(this, child, index);
        }
    }

    protected void notifyChildRemoved(Object child, int index) {
        for(ChangeListener listener : listeners) {
            listener.onChildRemoved(this, child, index);
        }
    }

    protected void notifyChildrensRemoved() {
        for(ChangeListener listener : listeners) {
            listener.onChildrensRemoved(this);
        }
    }

    @Override
    public void insert(MutableTreeNode newChild, int childIndex) {
        super.insert(newChild, childIndex);
        notifyChildAdded(newChild, childIndex);
    }

    @Override
    public void remove(MutableTreeNode aChild) {
        int index = children.indexOf(aChild);
        super.remove(aChild);
        notifyChildRemoved(aChild, index);
    }

    @Override
    public void removeAllChildren() {
        super.removeAllChildren();
        notifyChildrensRemoved();
    }

    @Override
    public void add(MutableTreeNode newChild) {
        super.add(newChild);
        notifyChildAdded(newChild, children.size() -1);
    }
}
