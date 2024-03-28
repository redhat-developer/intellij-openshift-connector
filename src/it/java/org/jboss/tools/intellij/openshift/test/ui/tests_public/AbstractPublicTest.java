/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Martin Szuc
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.tests_public;

import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.utils.KubeConfigUtility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * @author Martin Szuc
 * Abstract test class for common methods regarding public tests
 */
public class AbstractPublicTest extends AbstractBaseTest {

    private static boolean isLoggedOut = false;

    @BeforeAll
    public static void setUp() {
        if (!isLoggedOut) {
            backUpAndLogOut();
        }
    }

    @AfterAll
    public static void tearDown() {
        if (isLoggedOut) {
            KubeConfigUtility.restoreKubeConfig();
        }
    }

    private static void backUpAndLogOut() {
        KubeConfigUtility.backupKubeConfig();
        logOut();
        isLoggedOut = true;
    }
}
