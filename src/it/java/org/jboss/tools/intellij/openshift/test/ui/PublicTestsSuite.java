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

import org.jboss.tools.intellij.openshift.test.ui.tests_public.AboutPublicTest;
import org.jboss.tools.intellij.openshift.test.ui.tests_public.ClusterLoginDialogPublicTest;
import org.jboss.tools.intellij.openshift.test.ui.tests_public.GettingStartedTest;
import org.jboss.tools.intellij.openshift.test.ui.tests_public.OpenshiftExtensionTest;
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
    static void setUp() {
        KubeConfigUtility.backupKubeConfig();
        KubeConfigUtility.removeKubeConfig();
    }

    @AfterAll
    static void tearDown() {
        KubeConfigUtility.restoreKubeConfig();
    }
}
