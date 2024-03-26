/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
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
import com.intellij.remoterobot.fixtures.ContainerFixture;
import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.GettingStartedView;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * @author Martin Szuc
 * Abstract test class for common methods regarding public tests
 */
public class AbstractPublicTest extends AbstractBaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPublicTest.class);

    @AfterEach
    public void afterEachCleanUp() {
        cleanUpRunWindow();
        cleanUpOpenShift();
        cleanUpGettingStarted();
        cleanUpDialog();
    }

    private static void cleanUpRunWindow() {
        try {
            LOGGER.info("After test cleanup: Checking for any opened run window");
            robot.find(ComponentFixture.class, byXpath(XPathConstants.HIDE_BUTTON))
                    .click();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No run window opened");
        }
    }

    private static void cleanUpOpenShift() {
        try {
            LOGGER.info("After test cleanup: Checking for opened OpenShift view");
            OpenshiftView view = robot.find(OpenshiftView.class);
            robot.find(ComponentFixture.class, byXpath(XPathConstants.OPENSHIFT_BASELABEL));
            view.closeView();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No OpenShift view opened");
        }
    }

    private static void cleanUpGettingStarted() {
        try {
            LOGGER.info("After test cleanup: Checking for opened Getting Started view");
            GettingStartedView view = robot.find(GettingStartedView.class);
            robot.find(ComponentFixture.class, byXpath(XPathConstants.GETTING_STARTED_BASELABEL));
            view.closeView();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No Getting Started view opened");
        }
    }

    private static void cleanUpDialog() {
        try {
            LOGGER.info("After test cleanup: Checking for any opened dialog window");
            ContainerFixture dialogWindow = robot.find(ContainerFixture.class, byXpath(XPathConstants.MYDIALOG_CLASS));
            dialogWindow.find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CLASS))
                    .click();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No dialog window opened");
        }
    }
}
