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
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.*;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class DebugComponentAction extends OdoAction {
    public DebugComponentAction() {
        super(ComponentNode.class);
    }

    @Override
    public boolean isVisible(Object selected) {
        //TODO make invisible when already debugging a component to the same port
        boolean visible = super.isVisible(selected);
        if (visible) {
            ComponentNode componentNode = (ComponentNode) selected;
            Component component = (Component) componentNode.getUserObject();
            return (isPushed(component) && isDebuggable(componentNode, component));
        }
        return false;
    }

    private boolean isDebuggable(ComponentNode componentNode, Component component) {
        ApplicationNode applicationNode = (ApplicationNode) componentNode.getParent();
        try {
            Odo odo = ((ApplicationsRootNode) componentNode.getRoot()).getOdo();
            LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
            ApplicationsRootNode root = (ApplicationsRootNode) applicationNode.getRoot();
            ComponentInfo info = odo.getComponentInfo(root.getClient(), projectNode.toString(), applicationNode.toString(), component.getName());
            return info.getComponentTypeName().equals("java") || info.getComponentTypeName().equals("nodejs");
        } catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Debug"));
        }
        return false;
    }

    private boolean isPushed(Component component) {
        return component.getState() == ComponentState.PUSHED;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = (Component) componentNode.getUserObject();
        ApplicationNode applicationNode = (ApplicationNode) componentNode.getParent();
        LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
        ExecHelper.submit(() -> {
            try {
                odo.debug(projectNode.toString(), applicationNode.toString(), component.getPath(), component.getName(), null);
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Debug"));
            }
        });
    }
}
