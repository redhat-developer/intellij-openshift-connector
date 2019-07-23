package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;

public class ContextAwareComponentAction extends OdoAction {
    public ContextAwareComponentAction() {
        super(ComponentNode.class);
    }

    public ContextAwareComponentAction(Class... filters) {
        super(filters);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if (e.getPresentation().isVisible()) {
            LazyMutableTreeNode node = (LazyMutableTreeNode) getTree(e).getSelectionModel().getSelectionPath().getLastPathComponent();
            if (node instanceof ComponentNode) {
                Component component = (Component) node.getUserObject();
                e.getPresentation().setVisible(component.hasContext());
            }
        }
    }
}
