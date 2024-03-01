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
package org.jboss.tools.intellij.openshift.actions.registry;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistryNode;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DeleteRegistryAction extends OdoAction {
  public DeleteRegistryAction() {
    super(DevfileRegistryNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "delete registry";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    DevfileRegistryNode registryNode = (DevfileRegistryNode) selected;
    if (Messages.NO == Messages.showYesNoDialog("Delete registry '" + registryNode.getName() + "'.\nAre you sure?", "Delete registry",
      Messages.getQuestionIcon())) {
      sendTelemetryResults(TelemetryResult.ABORTED);
    } else {
      ExecHelper.submit(() -> {
        try {
          odo.deleteDevfileRegistry(registryNode.getName());
          NodeUtils.fireRemoved(registryNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
        } catch (IOException e) {
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Delete Registry"));
        }
      });
    }
  }
}
