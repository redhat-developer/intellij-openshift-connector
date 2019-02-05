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
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.tree.TreePath;
import java.awt.Component;
import java.util.stream.Stream;

public abstract class TreeAction extends AnAction {


    private Class[] filters;

    public TreeAction(Class... filters) {
        this.filters = filters;
    }

    protected Tree getTree(AnActionEvent e) {
        return (Tree) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
    }

    @Override
    public void update(AnActionEvent e) {
        boolean visible = false;
        Component comp = getTree(e);

        if (comp instanceof Tree) {
            TreePath selectPath = ((Tree) comp).getSelectionModel().getSelectionPath();
            visible = Stream.of(filters).anyMatch(cl -> cl.isAssignableFrom(selectPath.getLastPathComponent().getClass()));
        }
        e.getPresentation().setVisible(visible);
    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Tree tree = getTree(anActionEvent);
        TreePath selectedPath = tree.getSelectionModel().getSelectionPath();
        Object selected = selectedPath.getLastPathComponent();
        actionPerformed(anActionEvent, selectedPath, selected);
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected);
}
