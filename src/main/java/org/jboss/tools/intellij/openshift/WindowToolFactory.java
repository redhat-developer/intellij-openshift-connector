/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;
import org.jboss.tools.intellij.openshift.tree.ClustersTreeModel;
import org.jboss.tools.intellij.openshift.tree.ClustersTreeNodeCellRenderer;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeNodeCellRenderer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;


public class WindowToolFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        JBPanel<JBPanel> panel = new JBPanel<>();
        panel.setLayout(new BorderLayout());
        Tree tree = new Tree(new ClustersTreeModel());
        tree.setCellRenderer(new ClustersTreeNodeCellRenderer());
        PopupHandler.installPopupHandler(tree, "org.jboss.tools.intellij.tree", ActionPlaces.UNKNOWN);
        panel.add(new JBScrollPane(tree), BorderLayout.CENTER);

        tree = new Tree(new ApplicationTreeModel(project));
        tree.setCellRenderer(new ApplicationTreeNodeCellRenderer());
        PopupHandler.installPopupHandler(tree, "org.jboss.tools.intellij.tree", ActionPlaces.UNKNOWN);
        panel.add(new JBScrollPane(tree), BorderLayout.PAGE_START);
        toolWindow.getContentManager().addContent(contentFactory.createContent(panel, "", false));
    }
}
