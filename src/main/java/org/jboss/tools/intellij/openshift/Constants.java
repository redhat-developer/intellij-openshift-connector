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
    public static final String ODO_CONFIG_YAML = ".odo/config.yaml";
    public static final String HOME_FOLDER = System.getProperty("user.home");

    public static final String MIGRATION_HELP_PAGE_URL = "https://github.com/redhat-developer/intellij-openshift-connector/wiki/Migration-to-v0.1.0";

    public static final String GROUP_DISPLAY_ID = "OpenShift Connector";

    public static final String CLUSTER_MIGRATION_TITLE = "Cluster migration";
    public static final String CLUSTER_MIGRATION_MESSAGE = "Some of the resources in cluster must be updated to work with latest release of OpenShift Connector plugin";
    public static final String CLUSTER_MIGRATION_ERROR_MESSAGE = "Errors while migrating cluster resources";
    public static final String COMPONENT_MIGRATION_TITLE = "Component migrated";
    public static final String COMPONENT_MIGRATION_MESSAGE = "This component must be undeployed before new version is pushed, because it was created and deployed with previous version of OpenShift Connector plugin";
    public static final String COMPONENT_MIGRATION_ERROR_MESSAGE = "Errors while undeploying migrated component";

    public static final String UPDATE_LABEL = "Update";
    public static final String UNDEPLOY_LABEL = "Undeploy";
    public static final String HELP_LABEL = "Help";

    public static final String OCP4_CONFIG_NAMESPACE = "openshift-config-managed";
    public static final String OCP4_CONSOLE_PUBLIC_CONFIG_MAP_NAME = "console-public";
    public static final String OCP4_CONSOLE_URL_KEY_NAME = "consoleURL";
    /**
     * Home sub folder for the plugin
     */
    public static final String PLUGIN_FOLDER = ".odo";

    public enum DebugStatus {
        RUNNING, NOT_RUNNING, UNKNOWN
    }
}
