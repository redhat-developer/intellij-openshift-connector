package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.tree.TreePath;
import java.awt.*;
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
            visible = Stream.of(filters).anyMatch(cl -> cl.equals(selectPath.getLastPathComponent().getClass()));
        }
        e.getPresentation().setVisible(visible);
    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Tree tree = getTree(anActionEvent);
        TreePath selectedPath = tree.getSelectionModel().getSelectionPath();
        Object selected = selectedPath.getLastPathComponent();
        actionPerformed(anActionEvent, selected);
    }

    abstract void actionPerformed(AnActionEvent anActionEvent, Object selected);
}
