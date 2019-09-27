package org.jboss.tools.intellij.openshift;

public class Constants {
    public static final String ODO_CONFIG_YAML = ".odo/config.yaml";
    public static String HOME_FOLDER = System.getProperty("user.home");

    public static final String MIGRATION_HELP_PAGE_URL = "https://github.com/redhat-developer/intellij-openshift-connect/wiki/Migration-to-v0.1.0";

    public static final String GROUP_DISPLAY_ID = "OpenShift";

    public static final String CLUSTER_MIGRATION_TITLE = "Cluster migration";
    public static final String CLUSTER_MIGRATION_MESSAGE = "Some of the resources in cluster must be updated to work with latest release of OpenShift Connector plugin";
    public static final String CLUSTER_MIGRATION_ERROR_MESSAGE = "Errors while migrating cluster resources";
    public static final String COMPONENT_MIGRATION_TITLE = "Component migrated";
    public static final String COMPONENT_MIGRATION_MESSAGE = "This component must be undeployed before new version is pushed, because it was created and deployed with previous version of OpenShift Connector plugin";
    public static final String COMPONENT_MIGRATION_ERROR_MESSAGE = "Errors while undeploying migrated component";

    public static final String UPDATE_LABEL = "Update";
    public static final String UNDEPLOY_LABEL = "Undeploy";
    public static final String HELP_LABEL = "Help";
}
