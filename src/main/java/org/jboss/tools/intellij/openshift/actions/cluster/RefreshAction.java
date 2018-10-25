package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.TreeAction;

import javax.swing.tree.TreePath;

public class RefreshAction extends TreeAction {
    public RefreshAction() {
        super(String.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
    }
}
