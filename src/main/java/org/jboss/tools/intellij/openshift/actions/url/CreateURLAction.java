/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.url;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.ui.url.CreateURLDialog;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentKind;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateURLAction extends OdoAction {
    public CreateURLAction() {
        super(ComponentNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = (Component) componentNode.getUserObject();
        LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) ((TreeNode) selected).getParent();
        LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
        CompletableFuture.runAsync(() -> {
            try {
                if (createURL(odo, projectNode.toString(), applicationNode.toString(), component)) {
                    componentNode.reload();
                }
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create URL"));
            }
        });
    }

    public static boolean createURL(Odo odo, String project, String application, Component component) throws IOException {
        CreateURLDialog dialog;
        if (ComponentKind.S2I.equals(component.getInfo().getComponentKind())) {
            List<Integer> ports = odo.getServicePorts(project, application, component.getName());
            if (!ports.isEmpty()) {
                dialog = UIHelper.executeInUI(() -> {
                    CreateURLDialog dialog1 = new CreateURLDialog(false);
                    dialog1.setPorts(ports);
                    dialog1.show();
                    return dialog1;
                });

            } else {
                throw new IOException("Can't create url for component without ports");
            }
        } else {
            dialog = UIHelper.executeInUI(() -> {
                CreateURLDialog dialog1 = new CreateURLDialog(true);
                dialog1.show();
                return dialog1;
            });
        }
        if (dialog.isOK()) {
            Integer port = dialog.getSelectedPort();
            String urlName = dialog.getName();
            boolean secure = dialog.isSecure();
            if (port != null) {
                odo.createURL(project, application, component.getPath(), component.getName(), urlName, port, secure);
                return true;
            }
        }
        return false;
    }
}
