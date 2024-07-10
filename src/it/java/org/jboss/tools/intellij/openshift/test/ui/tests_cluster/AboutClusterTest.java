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

import com.intellij.remoterobot.fixtures.ComponentFixture;
import org.jboss.tools.intellij.openshift.test.ui.tests_public.AboutPublicTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.*;

/**
 * @author Martin Szuc
 * Test class verifying functionality of About button when right-clicked on cluster URL item in Openshift view when logged in to cluster
 */
public class AboutClusterTest extends AbstractClusterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AboutClusterTest.class);

    @Test
    public void aboutLoggedInTest() {
        LOGGER.info("aboutLoggedInTest: Start");

        AboutPublicTest.selectAboutAndGetClipboardContent();
        AboutPublicTest.verifyClipboardContent("odo", "Server:", "Kubernetes:");

        // Close the "Run" tool window
        robot.find(ComponentFixture.class, byXpath(HIDE_BUTTON))
                .click();

        LOGGER.info("aboutLoggedInTest: End");

    }
}
