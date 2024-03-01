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

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class OpenConsoleAction extends LoggedInClusterAction {

  @Override
  public String getTelemetryActionName() {return "open console";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    try {
      String url = odo.consoleURL();
      BrowserUtil.open(url);
      sendTelemetryResults(TelemetryResult.SUCCESS);
    } catch (IOException e) {
      sendTelemetryError(e);
      Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Open Console Dashboard");
    }

  }
}
