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
package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import io.fabric8.kubernetes.api.model.NamedContext;
import org.jboss.tools.intellij.openshift.actions.TreeAction;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.tree.ClustersTreeModel;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class DeleteFromKubeConfigAction extends TreeAction {
    public DeleteFromKubeConfigAction() {
        super(NamedContext.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        NamedContext context = (NamedContext) selected;
        ClustersTreeModel model = (ClustersTreeModel) getTree(anActionEvent).getModel();
        if (Messages.NO == Messages.showYesNoDialog("Delete Cluster '" + context.getName() + "'.\nAre you sure?", "Delete Cluster",
            Messages.getQuestionIcon())) {
            return;
        }
        model.getConfig().getContexts().remove(context);
        try {
            ConfigHelper.saveKubeConfig(model.getConfig());
        } catch (IOException e) {
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete from Kubernetes config");
        }
    }
}
