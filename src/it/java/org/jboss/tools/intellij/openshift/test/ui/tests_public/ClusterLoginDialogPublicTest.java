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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Martin Szuc
 * Test class verifying functionality of cluster login dialog
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClusterLoginDialogPublicTest extends AbstractBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterLoginDialogPublicTest.class);

    @Test
    @Order(1)
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

    @Test
    @Order(2)
    public void pasteTokenLoginCommandTest() {
        LOGGER.info("pasteTokenLoginCommandTest: Start");

        // Set the clipboard content with a token-based login command
        String tokenLoginCommand = "oc login --token=sampleToken123 --server=https://api.sample-url.com";
        StringSelection stringSelection = new StringSelection(tokenLoginCommand);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

        ClusterLoginDialog clusterLoginDialog = ClusterLoginDialog.open(robot);
        clusterLoginDialog.clickPasteLoginCommand();

        // Verify the text fields are correctly filled
        assertEquals("https://api.sample-url.com", clusterLoginDialog.getURL());
        assertEquals("sampleToken123", clusterLoginDialog.getToken());

        assertEquals("", clusterLoginDialog.getUsername());
        assertEquals("", clusterLoginDialog.getPassword());

        LOGGER.info("Closing cluster login dialog");
        clusterLoginDialog.close();
        LOGGER.info("pasteTokenLoginCommandTest: End");
    }
}