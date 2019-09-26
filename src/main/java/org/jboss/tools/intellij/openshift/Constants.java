package org.jboss.tools.intellij.openshift;

public class Constants {
    public static final String ODO_CONFIG_YAML = ".odo/config.yaml";

    public static final String GROUP_DISPLAY_ID = "OpenShift";
    public static String HOME_FOLDER = System.getProperty("user.home");

    public static final String MIGRATION_TITLE = "Cluster migration";
    public static final String MIGRATION_MESSAGE = "Some of the resources in cluster must be updated to work with latest release of OpenShift Connector plugin";
    public static final String MIGRATION_ERROR_MESSAGE = "Errors while migrating cluster resources";

    public static final String UPDATE_LABEL = "Update";
    public static final String HELP_LABEL = "Help";

}
