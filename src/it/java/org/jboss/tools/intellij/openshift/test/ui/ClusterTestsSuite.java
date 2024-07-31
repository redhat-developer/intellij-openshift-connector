package org.jboss.tools.intellij.openshift.test.ui;

import org.jboss.tools.intellij.openshift.test.ui.tests_cluster.*;
import org.jboss.tools.intellij.openshift.test.ui.utils.KubeConfigUtility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        LoginClusterTest.class,
        AboutClusterTest.class,
        ProjectClusterTest.class,
        ComponentNodeTest.class,
        CreateServiceTest.class
})
@IncludeClassNamePatterns({"^.*Test$"})
public class ClusterTestsSuite {
    @BeforeAll
    public static void setUp() {
        KubeConfigUtility.backupKubeConfig();
    }

    @AfterAll
    public static void tearDown() {
        KubeConfigUtility.restoreKubeConfig();
    }
}
