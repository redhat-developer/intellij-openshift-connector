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
package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Szuc
 * Test class verifying functionality of Log in to Cluster
 */
public class LoginClusterTest extends AbstractClusterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginClusterTest.class);

    @Test
    public void usernameLoginTest() {
        LOGGER.info("usernameLoginTest: Start");
        logOut();
        loginWithUsername();
        verifyClusterLogin(currentClusterUrl);
        LOGGER.info("usernameLoginTest: End");
    }
}
