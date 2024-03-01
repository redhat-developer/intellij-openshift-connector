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
package org.jboss.tools.intellij.openshift.actions.url;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.URLNode;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class OpenInBrowserAction extends OdoAction {
  public OpenInBrowserAction() {
    super(URLNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "open URL";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    BrowserUtil.open(getURL(((URLNode) selected).getUrl()));
    sendTelemetryResults(TelemetryResult.SUCCESS);
  }

  protected String getURL(URL url) {
    return url.asURL();
  }
}
