package org.jboss.tools.intellij.openshift.actions.component;

import org.jboss.tools.intellij.openshift.actions.OdoAction;
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
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            if (selected instanceof ComponentNode) {
                Component component = (Component) ((ComponentNode)selected).getUserObject();
                visible = component.hasContext();
            }
        }
        return visible;
    }
}
