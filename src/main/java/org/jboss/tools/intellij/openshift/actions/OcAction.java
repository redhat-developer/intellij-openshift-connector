/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.utils.oc.Oc;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;

public abstract class OcAction extends TelemetryAction {

  protected OcAction(Class... filters) {
    super(filters);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
    setTelemetrySender(new TelemetrySender(PREFIX_ACTION + getTelemetryActionName()));
    ActionUtils.getApplicationRootNode(anActionEvent).getOcTool()
      .whenComplete(
        (ocTool, throwable) -> {
          if (ocTool != null) {
            Oc oc = ocTool.get();
            if (oc != null) {
              this.actionPerformedOnSelectedObject(anActionEvent, getElement(selected), oc);
            }
          }
        }
      );
  }

  public abstract void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Oc oc);

}
