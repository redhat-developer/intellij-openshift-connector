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
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryHandler;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;

public abstract class HelmAction extends StructureTreeAction implements TelemetryHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(HelmAction.class);

  protected TelemetrySender telemetrySender;

  protected HelmAction(Class... filters) {
    super(filters);
  }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        telemetrySender = new TelemetrySender(PREFIX_ACTION + getTelemetryActionName());
        Helm helm = getHelm(anActionEvent);
        if (helm == null) {
          return;
        }
        this.actionPerformed(anActionEvent, (Object) getElement(selected), helm);
    }

    private Helm getHelm(AnActionEvent anActionEvent) {
        try {
          return ActionUtils.getApplicationRootNode(anActionEvent).getHelm(true).getNow(null);
        } catch(Exception e) {
          LOGGER.warn("Could not get helm: " + e.getMessage(), e);
          return null;
        }
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, Object selected, @NotNull Helm helm);

    protected abstract String getTelemetryActionName();

    public void sendTelemetryResults(TelemetryService.TelemetryResult result) {
        telemetrySender.sendTelemetryResults(result);
    }

    @Override
    public void sendTelemetryError(String message) {
        telemetrySender.sendTelemetryError(message);
    }

    @Override
    public void sendTelemetryError(Exception exception) {
        telemetrySender.sendTelemetryError(exception);
    }
}
