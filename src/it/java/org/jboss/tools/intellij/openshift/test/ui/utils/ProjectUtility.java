/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
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
import com.intellij.remoterobot.fixtures.JTextFieldFixture;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.FlatWelcomeFrame;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.information.TipDialog;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.project.NewProjectDialogWizard;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.common.ImportProjectFromVersionControlFixture;
import org.jboss.tools.intellij.openshift.test.ui.common.ProjectTreeFixture;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * @author Ondrej Dockal, Oleksii Korniienko, Ihor Okhrimenko
 */
public class ProjectUtility {

    public static void importProject(RemoteRobot robot, String projectName) {
        final ImportProjectFromVersionControlFixture importProjectFromVersionControlFixture = robot
                .find(ImportProjectFromVersionControlFixture.class);
        importProjectFromVersionControlFixture.typeUrl();
        importProjectFromVersionControlFixture.clickCloneButton();

        final ProjectTreeFixture projectTreeFixture = robot.find(ProjectTreeFixture.class, Duration.ofSeconds(60));
        projectTreeFixture.waitItemInTree(projectName, 180, 10);
    }

    public static void createEmptyProject(RemoteRobot robot, String projectName) {
        final FlatWelcomeFrame flatWelcomeFrame = robot.find(FlatWelcomeFrame.class);
        flatWelcomeFrame.createNewProject();
        final NewProjectDialogWizard newProjectDialogWizard = flatWelcomeFrame.find(NewProjectDialogWizard.class, Duration.ofSeconds(20));
        selectNewProjectType(robot, "Empty Project");
        JTextFieldFixture textField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='untitled']"));
        textField.setText(projectName);
        newProjectDialogWizard.finish();

        final IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
        //ideStatusBar.waitUntilProjectImportIsComplete(); TODO fix on IJ ULTIMATE 2023.2
    }

    public static void selectNewProjectType(RemoteRobot robot, String projectType) {
        ComponentFixture newProjectTypeList = robot.findAll(ComponentFixture.class, byXpath("JBList", "//div[contains(@visible_text, 'FX')]")).get(0);
        newProjectTypeList.findText(projectType).click();
    }

    public static void closeTipDialogIfItAppears(RemoteRobot robot) {
        try {
            TipDialog tipDialog = robot.find(TipDialog.class, Duration.ofSeconds(10));
//            tipDialog.close(); // temporary commented
            robot.find(ComponentFixture.class, byXpath("//div[@accessiblename='Close' and @class='JButton' and @text='Close']"), Duration.ofSeconds(5)).click(); // temporary workaround
        } catch (WaitForConditionTimeoutException e) {
            // no dialog appeared, no need to exception handling
        }
    }

    public static void closeGotItPopup(RemoteRobot robot) {
        try {
            robot.find(ComponentFixture.class, byXpath("JBList", "//div[@accessiblename='Got It' and @class='JButton' and @text='Got It']"), Duration.ofSeconds(10)).click();
        } catch (WaitForConditionTimeoutException e) {
            // no dialog appeared, no need to exception handling
        }
    }

    public static void sleep(long ms) {
        System.out.println("Putting thread into sleep for: " + ms + " ms");
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
