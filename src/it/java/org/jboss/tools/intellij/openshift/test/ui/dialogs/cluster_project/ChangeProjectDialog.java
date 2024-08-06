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
package org.jboss.tools.intellij.openshift.test.ui.dialogs.cluster_project;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.CommonContainerFixture;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.utils.Keyboard;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Change Active Project dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.MYDIALOG_CLASS)
@FixtureName(name = "Change Active Project Dialog")
public class ChangeProjectDialog extends CommonContainerFixture {

    public ChangeProjectDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void enterProjectName(RemoteRobot remoteRobot, String projectName) {
        // Focus the text field
        find(ComponentFixture.class, byXpath(XPathConstants.TEXT_FIELD_W_AUTO_COMPLETION)).click();

        // Use RemoteRobot's Keyboard to type the project name
        Keyboard keyboard = new Keyboard(remoteRobot);
        keyboard.enterText(projectName);
        if (isDropdownMenuOpened()) {
            keyboard.enter();   // Hide the dropdown menu with suggestions
        }
    }

    public void clickChange() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CHANGE)).click();
    }

    private boolean isDropdownMenuOpened() {
        try {
            this.find(ComponentFixture.class, byXpath(XPathConstants.HEAVY_WEIGHT_WINDOW));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isDropdownMenuOpened() {
        try {
            this.find(ComponentFixture.class, byXpath(XPathConstants.HEAVY_WEIGHT_WINDOW));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}