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
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateComponentAction extends OdoAction {
  public CreateComponentAction() {
    super(ApplicationNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) selected;
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        CreateComponentDialog dialog = UIHelper.executeInUI(() -> {
          try {
            return showDialog(odo.getComponentTypes());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
        if (dialog.isOK()) {
          createComponent(odo, projectNode.toString(), applicationNode.toString(), dialog);
          applicationNode.reload();
          if (dialog.getSourceType() == 0) {
              odo.push(projectNode.toString(), applicationNode.toString(), dialog.getName());
          }
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create component", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  private void createComponent(Odo odo, String project, String application, CreateComponentDialog dialog) throws IOException{
    if (dialog.getSourceType() == 0) {
      odo.createComponentLocal(project, application, dialog.getComponentType(), dialog.getComponentVersion(), dialog.getName(), dialog.getSource());
    } else {
      odo.createComponentGit(project, application, dialog.getComponentType(), dialog.getComponentVersion(), dialog.getName(), dialog.getSource());
    }
  }

  protected CreateComponentDialog showDialog(List<ComponentType> types) {
    CreateComponentDialog dialog = new CreateComponentDialog(null);
    dialog.setComponentTypes(types.toArray(new ComponentType[types.size()]));
    dialog.show();
    return dialog;
  }

}
