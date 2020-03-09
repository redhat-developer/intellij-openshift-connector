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
package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;
import java.awt.Component;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ListServicesAction extends LoggedInClusterAction {

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Component comp = getTree(e);
        if (comp instanceof Tree) {
            TreePath selectPath = ((Tree) comp).getSelectionModel().getSelectionPath();
            Object selected = selectPath.getLastPathComponent();
            boolean visible = e.getPresentation().isVisible();
            if (visible) {
                try {
                    ApplicationsRootNode rootNode = (ApplicationsRootNode) selected;
                    Odo odo = rootNode.getOdo();
                    visible = odo.isServiceCatalogAvailable();
                } catch (IOException | NullPointerException ex) {
                    //silently catch the exception to make the action not visible
                    visible = false;
                }
            }
            e.getPresentation().setVisible(visible);
        }
    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
        CompletableFuture.runAsync(() -> {
            try {
                odo.listServices();
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "List services"));
            }
        });
    }
}
