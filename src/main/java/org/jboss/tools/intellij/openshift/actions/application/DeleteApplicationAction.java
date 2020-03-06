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
package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DeleteApplicationAction extends OdoAction {
  public DeleteApplicationAction() {
    super(ApplicationNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) selected;
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    if (Messages.NO == Messages.showYesNoDialog("Delete Application '" + applicationNode.toString() + "'.\nAre you sure?", "Delete Application",
        Messages.getQuestionIcon())) {
        return;
    }
    CompletableFuture.runAsync(() -> {
      try {
        odo.deleteApplication(projectNode.toString(), applicationNode.toString());
        projectNode.remove(applicationNode);
      } catch (IOException e) {
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete application"));
      }
    });
  }
}

