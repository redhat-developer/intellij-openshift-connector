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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DescribeServiceAction extends OdoAction {
  public DescribeServiceAction() {
    super(ServiceNode.class);
  }

  @Override
  protected String getTelemetryActionName() { return "describe service"; }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
    Project project = getEventProject(anActionEvent);
    ServiceNode serviceNode = (ServiceNode) selected;
    NamespaceNode namespaceNode = serviceNode.getParent();
    runWithProgress((ProgressIndicator progress) -> {
      try {
        String template = odo.getServiceTemplate(namespaceNode.getName(), serviceNode.getName());
        odo.describeServiceTemplate(template);
        sendTelemetryResults(TelemetryResult.SUCCESS);
      } catch (IOException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Describe service"));
      }
    },
    "Describing Service " + serviceNode.getName(),
    project);
  }
}
