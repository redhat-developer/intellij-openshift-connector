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
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.cluster.LoginDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class LoginAction extends LoggedOutClusterAction {

    @Override
    protected String getTelemetryActionName() { return "login to cluster"; }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) selected;
    CompletableFuture.runAsync(() -> {
        try {
          LoginDialog loginDialog = UIHelper.executeInUI(() -> {
            LoginDialog dialog = new LoginDialog(null, clusterNode.getOdo().getMasterUrl().toString());
            dialog.show();
            return dialog;
            });
          if (loginDialog.isOK()) {
            odo.login(loginDialog.getClusterURL(), loginDialog.getUserName(), loginDialog.getPassword(), loginDialog.getToken());
            sendTelemetryResults(TelemetryResult.SUCCESS);
          } else {
            sendTelemetryResults(TelemetryResult.ABORTED);
          }
        } catch (IOException e) {
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Login"));
        }
      });
  }
}
