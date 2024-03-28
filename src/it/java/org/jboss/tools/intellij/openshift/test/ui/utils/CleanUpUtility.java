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
package org.jboss.tools.intellij.openshift.test.ui.utils;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.GettingStartedView;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * @author Martin Szuc
 * Utility class for closing various windows, dialogs, tool panes
 */
public class CleanUpUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanUpUtility.class);

    public static void cleanUpAll(RemoteRobot robot){
        cleanUpRunWindow(robot);
        cleanUpOpenShift(robot);
        cleanUpGettingStarted(robot);
        cleanUpDialog(robot);
    }


    public static void cleanUpRunWindow(RemoteRobot robot) {
        try {
            LOGGER.info("After test cleanup: Checking for any opened run window");
            robot.find(ComponentFixture.class, byXpath(XPathConstants.HIDE_BUTTON))
                    .click();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No run window opened");
        }
    }

    public static void cleanUpOpenShift(RemoteRobot robot) {
        try {
            LOGGER.info("After test cleanup: Checking for opened OpenShift view");
            OpenshiftView view = robot.find(OpenshiftView.class);
            robot.find(ComponentFixture.class, byXpath(XPathConstants.OPENSHIFT_BASELABEL));
            view.closeView();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No OpenShift view opened");
        }
    }

    public static void cleanUpGettingStarted(RemoteRobot robot) {
        try {
            LOGGER.info("After test cleanup: Checking for opened Getting Started view");
            GettingStartedView view = robot.find(GettingStartedView.class);
            robot.find(ComponentFixture.class, byXpath(XPathConstants.GETTING_STARTED_BASELABEL));
            view.closeView();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No Getting Started view opened");
        }
    }

    public static void cleanUpDialog(RemoteRobot robot) {
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
