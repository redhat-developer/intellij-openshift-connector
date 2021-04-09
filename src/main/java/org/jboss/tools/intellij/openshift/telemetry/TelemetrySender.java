package org.jboss.tools.intellij.openshift.telemetry;

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.jetbrains.annotations.NotNull;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult.ERROR;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.VALUE_ABORTED;

/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
public class TelemetrySender implements TelemetryHandler {

    private final TelemetryMessageBuilder.ActionMessage telemetry;

    public TelemetrySender(String actionName) {
        telemetry = TelemetryService.instance().getBuilder().action(actionName);
    }

    @Override
    public void sendTelemetryResults(TelemetryService.TelemetryResult result) {
        switch (result) {
            case SUCCESS:
                telemetry.success();
                break;
            case ABORTED:
                telemetry.result(VALUE_ABORTED);
                break;
            case ERROR:
            default:
                break;
        }
        telemetry.send();
    }

    public void sendTelemetryError(String message) {
        error(message);
        sendTelemetryResults(ERROR);
    }

    public void sendTelemetryError(Exception exception) {
        error(exception);
        sendTelemetryResults(ERROR);
    }

    public void addProperty(String property, @NotNull String value) {
        telemetry.property(property, value);
    }

    public void error(Exception exception) {
        telemetry.error(exception);
    }

    public void error(String message) {
        telemetry.error(message);
    }
}
