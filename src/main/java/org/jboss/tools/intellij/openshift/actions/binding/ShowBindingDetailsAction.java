/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.binding;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.BindingNode;
import org.jboss.tools.intellij.openshift.ui.binding.BindingDetailDialog;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

public class ShowBindingDetailsAction extends OdoAction {
  public ShowBindingDetailsAction() {
    super(BindingNode.class);
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    BindingNode node = (BindingNode) selected;
    BindingDetailDialog dialog = new BindingDetailDialog(anActionEvent.getProject(), null, node.getBinding());
    dialog.show();
    sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
  }

  @Override
  public String getTelemetryActionName() {
    return "show binding";
  }
}
