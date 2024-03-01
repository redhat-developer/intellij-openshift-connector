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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.ActionUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistriesNode;
import org.jboss.tools.intellij.openshift.ui.registry.CreateRegistryDialog;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileRegistry;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateRegistryAction extends OdoAction {
  public CreateRegistryAction() {
    super(DevfileRegistriesNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "create registry";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    DevfileRegistriesNode registriesNode = (DevfileRegistriesNode) selected;
    try {
      List<DevfileRegistry> registries = ProgressManager.getInstance().
        runProcessWithProgressSynchronously(odo::listDevfileRegistries, "Retrieving Registries", true, anActionEvent.getProject());
      CreateRegistryDialog dialog = new CreateRegistryDialog(registries);
      dialog.show();
      if (dialog.isOK()) {
        createDevfileRegistry(anActionEvent, dialog, registriesNode, odo);
      } else {
        sendTelemetryResults(TelemetryResult.ABORTED);
      }
    } catch (IOException e) {
      sendTelemetryError(e);
      Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create Registry");
    }
  }

  private void createDevfileRegistry(AnActionEvent anActionEvent, CreateRegistryDialog dialog, DevfileRegistriesNode registriesNode, Odo odo) {
    ExecHelper.submit(() -> {
      try {
        odo.createDevfileRegistry(dialog.getName(), dialog.getURL(), dialog.getToken());
        ActionUtils.getApplicationTreeStructure(anActionEvent).fireModified(registriesNode);
        sendTelemetryResults(TelemetryResult.SUCCESS);
      } catch (IOException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(),
          "Create registry"));
      }
    });
  }
}
