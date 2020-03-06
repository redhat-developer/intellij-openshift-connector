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
package org.jboss.tools.intellij.openshift.actions.service;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.ui.service.CreateServiceDialog;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateServiceAction extends OdoAction {

    public CreateServiceAction() {
        super(ApplicationNode.class, ProjectNode.class);
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            ApplicationsRootNode rootNode = (ApplicationsRootNode) ((LazyMutableTreeNode) selected).getRoot();
            if (rootNode != null) {
                try {
                    Odo odo = rootNode.getOdo();
                    return odo.isServiceCatalogAvailable();
                } catch (IOException ex) {
                    //silently catch the exception to make the action not visible
                }
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
        final String applicationName;
        String projectName;
        if (selected instanceof ApplicationNode) {
            applicationName = selected.toString();
            projectName = ((LazyMutableTreeNode) selected).getParent().toString();
        } else { // selected is ProjectNode
            applicationName = "";
            projectName = selected.toString();
        }
        CompletableFuture.runAsync(() -> {
            try {
                List<ServiceTemplate> templates = odo.getServiceTemplates();
                if (!templates.isEmpty()) {
                    CreateServiceDialog dialog = UIHelper.executeInUI(() -> showDialog(templates, applicationName));
                    if (dialog.isOK()) {
                        createService(odo, projectName, dialog.getApplication(), dialog);
                        ((LazyMutableTreeNode) selected).reload();
                    }
                } else {
                    UIHelper.executeInUI(() -> Messages.showWarningDialog("No templates available", "Create service"));
                }
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create service"));
            }
        });
    }

    private void createService(Odo odo, String project, String application, CreateServiceDialog dialog) throws IOException {
        odo.createService(project, application, dialog.getServiceTemplate().getName(), dialog.getServiceTemplate().getPlan(), dialog.getName());
    }

    protected CreateServiceDialog showDialog(List<ServiceTemplate> templates, String application) {
        CreateServiceDialog dialog = new CreateServiceDialog(null);
        dialog.setServiceTemplates(templates.toArray(new ServiceTemplate[templates.size()]));
        if (StringUtils.isNotEmpty(application)) {
            dialog.setApplication(application);
        }
        dialog.show();
        return dialog;
    }

}
