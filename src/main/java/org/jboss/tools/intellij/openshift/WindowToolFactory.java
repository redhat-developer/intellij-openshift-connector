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
import com.intellij.openapi.actionSystem.ActionManager;
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
import com.intellij.util.concurrency.Invoker;
import com.redhat.devtools.intellij.common.tree.MutableModelSynchronizer;
import com.redhat.devtools.intellij.common.tree.TreeHelper;
import com.redhat.devtools.intellij.common.utils.IDEAContentFactory;
import java.awt.BorderLayout;
import java.util.List;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jetbrains.annotations.NotNull;


public class WindowToolFactory implements ToolWindowFactory {

  public static final String ACTION_ID = "org.jboss.tools.intellij.openshift.actions.toolwindow.FeedBackAction";
  public static final String TOOLWINDOW_ID = "org.jboss.tools.intellij.tree";

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ContentFactory contentFactory = IDEAContentFactory.getInstance();
    JBPanel<JBPanel> panel = new JBPanel<>();
    panel.setLayout(new BorderLayout());
    Content content = contentFactory.createContent(panel, "", false);
    ApplicationsTreeStructure structure = new ApplicationsTreeStructure(project, content);
    StructureTreeModel<ApplicationsTreeStructure> model = new StructureTreeModel<>(
      structure,
      null,
      Invoker.forBackgroundPoolWithoutReadAction(content),
      content);
    content.setDisposer(structure);
    new MutableModelSynchronizer<>(model, structure, structure);
    Tree tree = new Tree(new AsyncTreeModel(model, content));
    tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
    tree.setCellRenderer(new NodeRenderer());
    tree.setRootVisible(false);
    PopupHandler.installPopupMenu(tree, TOOLWINDOW_ID, ActionPlaces.MAIN_MENU);
    panel.add(new JBScrollPane(tree), BorderLayout.CENTER);
    toolWindow.getContentManager().addContent(content);
    toolWindow.setTitleActions(
      List.of(ActionManager.getInstance().getAction(ACTION_ID)));
    TreeHelper.addLinkSupport(tree);
  }
}
