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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.cluster.LoginDialog;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class LoginAction extends OdoAction {

  public LoginAction() {
    super(ApplicationsRootNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "login to cluster";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) selected;
    runWithProgress((ProgressIndicator progress) -> {
        try {
          LoginDialog loginDialog = UIHelper.executeInUI(() -> {
            LoginDialog dialog = new LoginDialog(anActionEvent.getProject(), null, odo.getMasterUrl().toString());
            dialog.show();
            return dialog;
          });
          if (loginDialog.isOK()) {
            setProcessing("Logging in...", clusterNode);
            odo.login(
              loginDialog.getClusterURL(),
              loginDialog.getUserName(),
              loginDialog.getPassword(),
              loginDialog.getToken());
            sendTelemetryResults(TelemetryResult.SUCCESS);
          } else {
            sendTelemetryResults(TelemetryResult.ABORTED);
          }
        } catch (IOException e) {
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Login"));
        } finally {
          clearProcessing(clusterNode);
        }
      },
      "Logging in...",
      getEventProject(anActionEvent)
    );
  }
}
