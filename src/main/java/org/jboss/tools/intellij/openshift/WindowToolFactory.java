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

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.tree.MutableModelSynchronizer;
import com.redhat.devtools.intellij.common.tree.TreeHelper;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;


public class WindowToolFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        JBPanel<JBPanel> panel = new JBPanel<>();
        panel.setLayout(new BorderLayout());
        Content content = contentFactory.createContent(panel, "", false);
        ApplicationsTreeStructure structure = new ApplicationsTreeStructure(project);
        StructureTreeModel<ApplicationsTreeStructure> model = new StructureTreeModel<>(structure, content);
        new MutableModelSynchronizer<>(model, structure, structure);
        Tree tree = new Tree(new AsyncTreeModel(model, content));
        tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
        tree.setCellRenderer(new NodeRenderer());
        tree.setRootVisible(false);
        PopupHandler.installPopupHandler(tree, "org.jboss.tools.intellij.tree", ActionPlaces.UNKNOWN);
        panel.add(new JBScrollPane(tree), BorderLayout.CENTER);
        toolWindow.getContentManager().addContent(content);
        TreeHelper.addLinkSupport(tree);
    }
}
