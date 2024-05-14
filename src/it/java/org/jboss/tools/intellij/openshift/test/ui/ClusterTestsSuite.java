package org.jboss.tools.intellij.openshift.test.ui;

import org.jboss.tools.intellij.openshift.test.ui.tests_cluster.AboutClusterTest;
import org.jboss.tools.intellij.openshift.test.ui.tests_cluster.LoginClusterTest;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        LoginClusterTest.class,
        AboutClusterTest.class
})
@IncludeClassNamePatterns({"^.*Test$"})
public class ClusterTestsSuite {
}
