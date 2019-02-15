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
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.ui.tree.BaseTreeModel;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.RefreshableTreeModel;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.utils.ConfigWatcher;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ApplicationTreeModel extends BaseTreeModel<Object> implements ConfigWatcher.Listener, RefreshableTreeModel, LazyMutableTreeNode.ChangeListener {
    private ApplicationsRootNode ROOT;



    public ApplicationTreeModel() {
        CompletableFuture.runAsync(new ConfigWatcher(new File(ConfigHelper.getKubeConfigPath()), this));
        ROOT = new ApplicationsRootNode();
        ROOT.addChangeListener(this);
    }

    @Override
    public List<Object> getChildren(Object o) {
        if (o instanceof LazyMutableTreeNode) {
            LazyMutableTreeNode node = (LazyMutableTreeNode) o;
            if (!node.isLoaded()) {
                node.load();
            }
            return Collections.list(((MutableTreeNode)o).children());

        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public void onUpdate(ConfigWatcher source) {
        try {
            new ConfigBuilder().build();
            ExecHelper.executeAfter(this::refresh, 1, TimeUnit.SECONDS);
        } catch (Exception e) {}
    }

    @Override
    public void refresh() {
        TreePath path = new TreePath(ROOT);
        try {
            ROOT = new ApplicationsRootNode();
            ROOT.addChangeListener(this);
            this.treeStructureChanged(path, new int[0], new Object[0]);
        } catch (Exception e) {
        }
    }

    @Override
    public void onChildAdded(LazyMutableTreeNode source, Object child, int index) {
        if (child instanceof LazyMutableTreeNode) {
            ((LazyMutableTreeNode)child).addChangeListener(this);
        }
        treeNodesInserted(new TreePath(source.getPath()), new int[] { index }, new Object[] { child });
    }

    @Override
    public void onChildRemoved(LazyMutableTreeNode source, Object child, int index) {
        if (child instanceof LazyMutableTreeNode) {
            ((LazyMutableTreeNode)child).removeChangeListener(this);
        }
        treeNodesRemoved(new TreePath(source.getPath()), new int[] { index }, new Object[] { child });
    }

    @Override
    public void onChildrensRemoved(LazyMutableTreeNode source) {
        treeStructureChanged(new TreePath(source.getPath()), new int[0], new Object[0]);

    }
}
