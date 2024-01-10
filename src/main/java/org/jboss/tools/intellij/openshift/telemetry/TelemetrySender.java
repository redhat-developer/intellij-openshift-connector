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
package org.jboss.tools.intellij.openshift.telemetry;

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.asyncSend;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.instance;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult.ERROR;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.VALUE_ABORTED;

public class TelemetrySender implements TelemetryHandler {

    private static final Pattern CLUSTER_URL_PATTERN=Pattern.compile("http(.*)/apis",Pattern.CASE_INSENSITIVE);
    public static final String ANONYMOUS_CLUSTER_URL = "<CLUSTER_URL>";

    private static final Pattern TOKEN_PATTERN=Pattern.compile("token-(.*):([\\d]+)",Pattern.CASE_INSENSITIVE);
    public static final String ANONYMOUS_TOKEN = "<TOKEN>";

    private final TelemetryMessageBuilder.ActionMessage telemetry;

    public TelemetrySender(String actionName) {
        telemetry = instance().getBuilder().action(actionName);
    }

    @Override
    public void sendTelemetryResults(TelemetryResult result) {
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
        asyncSend(telemetry);
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
        // anonymize cluster address and token
        telemetry.error(anonymizeToken(anonymizeClusterUrl(message)));
    }

    public static String anonymizeClusterUrl(String string) {
        return string != null && !string.isEmpty() ? CLUSTER_URL_PATTERN.matcher(string).replaceAll(ANONYMOUS_CLUSTER_URL) : string;
    }

    public static String anonymizeToken(String string) {
        return string != null && !string.isEmpty() ? TOKEN_PATTERN.matcher(string).replaceAll(ANONYMOUS_TOKEN) : string;
    }
}
