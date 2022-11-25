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
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jetbrains.annotations.NotNull;

public class DebugNodeJSComponentAction extends DebugComponentAction {

    private static final String NODE_JS_LEGACY = "nodejs";
    private static final String NODE_JS = "Node.js";

    @Override
    protected boolean isDebuggable(@NotNull ComponentInfo componentInfo) {
        return NODE_JS.equals(componentInfo.getLanguage()) ||
                NODE_JS_LEGACY.equals(componentInfo.getLanguage()) ||
                componentInfo.getComponentTypeName().contains(NODE_JS) ||
                componentInfo.getComponentTypeName().contains(NODE_JS_LEGACY);
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
}
