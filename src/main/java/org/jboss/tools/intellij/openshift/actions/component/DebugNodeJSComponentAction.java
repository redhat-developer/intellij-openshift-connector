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
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.jetbrains.debugger.wip.JSRemoteDebugConfiguration;
import com.jetbrains.debugger.wip.JSRemoteDebugConfigurationType;
import org.jetbrains.annotations.NotNull;

public class DebugNodeJSComponentAction extends DebugComponentAction {

    private static final String NODE_JS = "nodejs";

    @Override
    protected boolean isDebuggable(@NotNull String componentTypeName) {
        return componentTypeName.contains(NODE_JS);
    }

    @Override
    protected String getDebugLanguage() {
        return NODE_JS;
    }

    @Override
    protected ConfigurationType getConfigurationType() {
        return new JSRemoteDebugConfigurationType();
    }

    @Override
    protected void initConfiguration(RunConfiguration configuration, Integer port) {
        if (configuration instanceof JSRemoteDebugConfiguration) {
            ((JSRemoteDebugConfiguration) configuration).setHost("localhost");
            ((JSRemoteDebugConfiguration) configuration).setPort(port);
        }
    }

    @Override
    protected int getPortFromConfiguration(RunConfiguration configuration) {
        if (configuration instanceof JSRemoteDebugConfiguration) {
            return ((JSRemoteDebugConfiguration) configuration).getPort();
        }
        return -1;
    }
}
