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
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeNodeCellRenderer;
import org.jboss.tools.intellij.openshift.tree.ClustersTreeModel;
import org.jboss.tools.intellij.openshift.tree.ClustersTreeNodeCellRenderer;
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
        panel.add(new JBScrollPane(tree), BorderLayout.PAGE_START);

        tree = new Tree(new ApplicationTreeModel());
        tree.setCellRenderer(new ApplicationTreeNodeCellRenderer(tree.getCellRenderer()));
        PopupHandler.installPopupHandler(tree, "org.jboss.tools.intellij.tree", ActionPlaces.UNKNOWN);
        panel.add(new JBScrollPane(tree), BorderLayout.CENTER);
        toolWindow.getContentManager().addContent(contentFactory.createContent(panel, "", false));
    }
}
