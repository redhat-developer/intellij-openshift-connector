package org.jboss.tools.intellij.openshift.test.ui;
import org.jboss.tools.intellij.openshift.test.ui.tests_public.*;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        GettingStartedTest.class,
        AboutPublicTest.class,
        ClusterLoginDialogPublicTest.class,
        OpenshiftExtensionTest.class
})
@IncludeClassNamePatterns({"^.*Test$"})
public class PublicTestsSuite {
}
