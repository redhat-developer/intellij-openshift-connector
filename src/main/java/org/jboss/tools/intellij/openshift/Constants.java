/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift;

public class Constants {
    public static final String HOME_FOLDER = System.getProperty("user.home");

    public static final String GROUP_DISPLAY_ID = "OpenShift Toolkit";

    public static final String OCP4_CONFIG_NAMESPACE = "openshift-config-managed";
    public static final String OCP4_CONSOLE_PUBLIC_CONFIG_MAP_NAME = "console-public";
    public static final String OCP4_CONSOLE_URL_KEY_NAME = "consoleURL";

    public static final String OCP3_CONFIG_NAMESPACE = "openshift-web-console";
    public static final String OCP3_WEBCONSOLE_CONFIG_MAP_NAME = "webconsole-config";
    public static final String OCP3_WEBCONSOLE_YAML_FILE_NAME = "webconsole-config.yaml";

    public static final String DEVFILE_NAME = "devfile.yaml";

    public static final String STRUCTURE_PROPERTY = Constants.class.getPackage().getName() + ".structure";

    /**
     * Home sub folder for the plugin
     */
    public static final String PLUGIN_FOLDER = ".odo";
    public static final String PLUGIN_ID = "org.jboss.tools.intellij.openshift";
    public static final String REDHAT_SSO_SERVER_ID = "org.jboss.tools.intellij.openshift.authorizationServer.redhat";

    public enum DebugStatus {
        RUNNING, NOT_RUNNING, UNKNOWN
    }
}
