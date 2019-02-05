/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.cluster.LoginDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LoginAction extends LoggedOutClusterAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) selected;
    CompletableFuture.runAsync(() -> {
        try {
          LoginDialog loginDialog = UIHelper.executeInUI(() -> {
            LoginDialog dialog = new LoginDialog(null, clusterNode.toString());
            dialog.show();
            return dialog;
            });
          if (loginDialog.isOK()) {
            odo.login(loginDialog.getClusterURL(), loginDialog.getUserName(), loginDialog.getPassword());
          }
        } catch (IOException e) {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Login", JOptionPane.ERROR_MESSAGE));
        }
      });
  }
}
