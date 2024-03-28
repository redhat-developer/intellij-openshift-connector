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

import com.intellij.remoterobot.fixtures.ComponentFixture;
import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.ClusterLoginDialog;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Martin Szuc
 * Test class verifying functionality of cluster login dialog
 */
public class ClusterLoginDialogPublicTest extends AbstractPublicTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterLoginDialogPublicTest.class);

    @Test
    public void clusterLoginDialogTest() {
        LOGGER.info("clusterLoginDialogTest: Start");
        ClusterLoginDialog clusterLoginDialog = ClusterLoginDialog.open(robot);

        assertTrue(robot.findAll(ComponentFixture.class, byXpath(XPathConstants.MYDIALOG_CLASS))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        LOGGER.info("Closing cluster login dialog");
        clusterLoginDialog.close();

        assertFalse(robot.findAll(ComponentFixture.class, byXpath(XPathConstants.MYDIALOG_CLASS))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
        LOGGER.info("clusterLoginDialogTest: End");

    }
}
