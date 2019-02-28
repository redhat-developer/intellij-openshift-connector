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
package org.jboss.tools.intellij.openshift.actions.service;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.ui.service.CreateServiceDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateServiceAction extends OdoAction {
  public CreateServiceAction() {
    super(ApplicationNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) selected;
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        List<ServiceTemplate> templates = odo.getServiceTemplates();
        if (!templates.isEmpty()) {
          CreateServiceDialog dialog = UIHelper.executeInUI(() -> {
              return showDialog(templates);
          });
          if (dialog.isOK()) {
            createService(odo, projectNode.toString(), applicationNode.toString(), dialog);
            applicationNode.reload();
          }
        } else {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "No templates available", "Create service", JOptionPane.WARNING_MESSAGE));
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create service", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  private void createService(Odo odo, String project, String application, CreateServiceDialog dialog) throws IOException{
    odo.createService(project, application, dialog.getServiceTemplate().getName(), dialog.getServiceTemplate().getPlan(), dialog.getName());
  }

  protected CreateServiceDialog showDialog(List<ServiceTemplate> templates) {
    CreateServiceDialog dialog = new CreateServiceDialog(null);
    dialog.setServiceTemplates(templates.toArray(new ServiceTemplate[templates.size()]));
    dialog.show();
    return dialog;
  }

}
