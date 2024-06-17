package org.jboss.tools.intellij.openshift.test.ui;
import org.jboss.tools.intellij.openshift.test.ui.tests_public.*;
import org.jboss.tools.intellij.openshift.test.ui.utils.KubeConfigUtility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        GettingStartedTest.class,
        OpenshiftExtensionTest.class,
        AboutPublicTest.class,
        ClusterLoginDialogPublicTest.class
})
@IncludeClassNamePatterns({"^.*Test$"})
public class PublicTestsSuite {
    @BeforeAll
    public static void setUp() {
        KubeConfigUtility.backupKubeConfig();
        KubeConfigUtility.removeKubeConfig();
    }

    @AfterAll
    public static void tearDown() {
        KubeConfigUtility.restoreKubeConfig();
    }
}
