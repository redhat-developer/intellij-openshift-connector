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

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.search.locators.Locator;
import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static com.redhat.devtools.intellij.commonuitest.utils.steps.SharedSteps.waitForComponentByXpath;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.*;

/**
 * @author Martin Szuc
 * Test class verifying functionality of About button when right-clicked on cluster URL item in Openshift view
 */
public class AboutPublicTest extends AbstractPublicTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AboutPublicTest.class);

    @Test
    public void aboutLoggedOutTest() {
        LOGGER.info("aboutLoggedOutTest: Start");

        selectAboutAndGetClipboardContent();
        verifyClipboardContent("odo version");
        // Close the "Run" tool window
        robot.find(ComponentFixture.class, byXpath(HIDE_BUTTON)).click();

        LOGGER.info("aboutLoggedOutTest: End");
    }

    public static void selectAboutAndGetClipboardContent() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        view.menuRightClickAndSelect(robot, 0, LabelConstants.ABOUT);
        waitForComponentByXpath(robot, 20, 1, byXpath(JB_TERMINAL_PANEL));

        sleep(2000);

        try {
            aboutTerminalRightClickSelect(robot, byXpath(SELECT_ALL));
            aboutTerminalRightClickSelect(robot, byXpath(COPY));
        } catch (Exception e) {
            LOGGER.error("An error occurred while selecting and copying text from the terminal: {}", e.getMessage());
            fail("Test failed due to an error while selecting and copying text from the terminal");
        }
    }

    public static void verifyClipboardContent(String... expectedContents) {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String clipboardContents;
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                clipboardContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                for (String expectedContent : expectedContents) {
                    if (!clipboardContents.contains(expectedContent)) {
                        // Log and fail the test if the expected content is not found
                        LOGGER.error("Test failed: Contents were not able to verify! Expected content missing from clipboard. Clipboard contents: { " + clipboardContents + " }, expected: " + expectedContent);
                        fail("Clipboard content verification failed. Expected content missing.");
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                LOGGER.error("Test failed: Copied text is not string or clipboard could not be accessed.");
                fail("Test failed due to clipboard not working correctly.");
            } catch (Exception e) {
                LOGGER.error("Test failed due to an unexpected error: " + e.getMessage());
                fail("Test failed due to an unexpected error.");
            }
        } else {
            LOGGER.error("Clipboard is empty or the content type is not supported.");
            fail("Clipboard is empty or the content type is not supported.");
        }
    }

    static public void aboutTerminalRightClickSelect(RemoteRobot robot, Locator xpath) {
        Point linkPosition = new Point(20, 20);
        ComponentFixture terminalPanel = robot.find(ComponentFixture.class, byXpath(JB_TERMINAL_PANEL));
        terminalPanel.rightClick(linkPosition);
        waitForComponentByXpath(robot, 20, 1, xpath);
        robot.find(ComponentFixture.class, xpath)
                .click();
    }

}
