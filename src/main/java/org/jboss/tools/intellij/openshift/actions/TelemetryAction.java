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

import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryHandler;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySenderAware;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TelemetryAction extends StructureTreeAction implements TelemetryHandler, TelemetrySenderAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryAction.class);

  private TelemetrySender telemetrySender;

  protected TelemetryAction(Class... filters) {
    super(filters);
  }

  @Override
  public void setTelemetrySender(TelemetrySender telemetrySender) {
    this.telemetrySender = telemetrySender;
  }

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

  public void addProperty(String property, @NotNull String value) {
    if (telemetrySender != null) {
      telemetrySender.addProperty(property, value);
    } else {
      LOGGER.warn("TelemetrySender is null. Cannot add property {} to Telemetry", property);
    }
  }
}
