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

import io.fabric8.kubernetes.client.Config;

public class Constants {
    public static final String HOME_FOLDER = System.getProperty("user.home");

    public static final String MIGRATION_HELP_PAGE_URL = "https://github.com/redhat-developer/intellij-openshift-connector/wiki/Migration-to-v0.1.0";

    public static final String GROUP_DISPLAY_ID = "OpenShift Toolkit";

    public static final String COMPONENT_MIGRATION_TITLE = "Component migrated";
    public static final String COMPONENT_MIGRATION_MESSAGE = "This component must be undeployed before new version is pushed, because it was created and deployed with previous version of OpenShift Toolkit plugin";

    public static final String UNDEPLOY_LABEL = "Undeploy";
    public static final String HELP_LABEL = "Help";

    public static final String OCP4_CONFIG_NAMESPACE = "openshift-config-managed";
    public static final String OCP4_CONSOLE_PUBLIC_CONFIG_MAP_NAME = "console-public";
    public static final String OCP4_CONSOLE_URL_KEY_NAME = "consoleURL";

    public static final String OCP3_CONFIG_NAMESPACE = "openshift-web-console";
    public static final String OCP3_WEBCONSOLE_CONFIG_MAP_NAME = "webconsole-config";
    public static final String OCP3_WEBCONSOLE_YAML_FILE_NAME = "webconsole-config.yaml";

    public static final String DEVFILE_NAME = "devfile.yaml";

    public static final String STRUCTURE_PROPERTY = Constants.class.getPackage().getName() + ".structure";

    public static final String DEFAULT_KUBE_URL = Config.DEFAULT_MASTER_URL.substring(8);

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
