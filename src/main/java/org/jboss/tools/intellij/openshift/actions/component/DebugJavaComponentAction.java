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
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import org.jetbrains.annotations.NotNull;


public class DebugJavaComponentAction extends DebugComponentAction {

    private static final String JAVA = "java";

    @Override
    protected boolean isDebuggable(@NotNull String componentTypeName) {
        return componentTypeName.contains(JAVA);
    }

    @Override
    protected String getDebugLanguage() {
        return JAVA;
    }

    @Override
    protected ConfigurationType getConfigurationType() {
        return RemoteConfigurationType.getInstance();
    }

    @Override
    protected void initConfiguration(RunConfiguration configuration, Integer port) {
        if (configuration instanceof RemoteConfiguration) {
            RemoteConfiguration remoteConfiguration = (RemoteConfiguration) configuration;
            remoteConfiguration.HOST = "localhost";
            remoteConfiguration.PORT = port.toString();
        }
    }

    @Override
    protected int getPortFromConfiguration(RunConfiguration configuration) {
        if (configuration instanceof RemoteConfiguration) {
            return Integer.parseInt(((RemoteConfiguration) configuration).PORT);
        }
        return -1;
    }

}
