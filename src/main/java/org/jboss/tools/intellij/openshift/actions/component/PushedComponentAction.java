package org.jboss.tools.intellij.openshift.actions.component;

import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;

public class PushedComponentAction extends ContextAwareComponentAction {
    @Override
    public boolean isVisible(Object selected) {
        boolean isVisible = super.isVisible(selected);
        if (isVisible) {
            isVisible = ((Component)((ComponentNode)selected).getUserObject()).getState() == ComponentState.PUSHED;
        }
        return isVisible;
    }
}
