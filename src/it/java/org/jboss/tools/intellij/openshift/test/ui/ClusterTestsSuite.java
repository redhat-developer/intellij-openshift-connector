/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui;

import org.jboss.tools.intellij.openshift.test.ui.tests_cluster.AboutClusterTest;
import org.jboss.tools.intellij.openshift.test.ui.tests_cluster.CreateComponentTest;
import org.jboss.tools.intellij.openshift.test.ui.tests_cluster.CreateServiceTest;
import org.jboss.tools.intellij.openshift.test.ui.tests_cluster.LoginClusterTest;
import org.jboss.tools.intellij.openshift.test.ui.tests_cluster.ProjectClusterTest;
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
        CreateComponentTest.class,
        CreateServiceTest.class
})
@IncludeClassNamePatterns({"^.*Test$"})
public class ClusterTestsSuite {
    @BeforeAll
    static void setUp() {
        KubeConfigUtility.backupKubeConfig();
    }

    @AfterAll
    static void tearDown() {
        KubeConfigUtility.restoreKubeConfig();
    }
}
