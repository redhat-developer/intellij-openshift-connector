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
import org.jboss.tools.intellij.openshift.utils.odo.ComponentKind;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class DebugNodeJSComponentAction extends DebugComponentAction {

    private static final Logger LOG = LoggerFactory.getLogger(DebugNodeJSComponentAction.class);

    private static final String NODE_JS = "nodejs";

    @Override
    protected boolean isDebuggable(ComponentKind kind, @NotNull String componentTypeName) {
        switch (kind) {
            case S2I:
                return NODE_JS.equals(componentTypeName);
            case DEVFILE:
                return componentTypeName.contains(NODE_JS);
        }
        return false;
    }

    @Override
    protected ConfigurationType getConfigurationType() {
        return new JSRemoteDebugConfigurationType();
    }

    @Override
    protected void initConfiguration(RunConfiguration configuration, Integer port) {
        if (configuration instanceof JSRemoteDebugConfiguration) {
            try {
                Field hostField = JSRemoteDebugConfiguration.class.getDeclaredField("host");
                hostField.setAccessible(true);
                hostField.set(configuration, "localhost");
                Field portField = JSRemoteDebugConfiguration.class.getDeclaredField("port");
                portField.setAccessible(true);
                portField.set(configuration, port);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    protected int getPortFromConfiguration(RunConfiguration configuration) {
        if (configuration instanceof JSRemoteDebugConfiguration) {
            Integer port;
            try {
                Field f = JSRemoteDebugConfiguration.class.getDeclaredField("port");
                f.setAccessible(true);
                port = (Integer) f.get(configuration);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return -1;
            }
            return port;
        }
        return -1;
    }
}
