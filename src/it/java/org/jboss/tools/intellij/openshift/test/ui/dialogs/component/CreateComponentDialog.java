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
package org.jboss.tools.intellij.openshift.test.ui.dialogs.component;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import com.intellij.remoterobot.utils.Keyboard;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Create Component dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.MYDIALOG_CLASS)
@FixtureName(name = "Create Component Dialog")
public class CreateComponentDialog extends CommonContainerFixture {

    public CreateComponentDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public static CreateComponentDialog open(RemoteRobot robot) {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.expandOpenshiftExceptDevfile();
        view.menuRightClickAndSelect(robot, 1, LabelConstants.NEW_COMPONENT);
        return robot.find(CreateComponentDialog.class, Duration.ofSeconds(60));
    }

    public void close() {
        findText(LabelConstants.CANCEL).click();
    }

    public void setName(String name) {
        JTextFieldFixture nameField = findAll(JTextFieldFixture.class, byXpath(XPathConstants.JTEXT_FIELD)).get(0);
        nameField.click();
        nameField.setText(name);
    }

    public void selectModule() {
        find(ComponentFixture.class, byXpath("//div[@text='Select module']")).click();
    }

    public void selectFolder() {
        find(ComponentFixture.class, byXpath("//div[@text='Select folder']")).click();
    }

    public void selectComponentType(String type, RemoteRobot remoteRobot) {
        // Find the JList
        JListFixture jList = find(JListFixture.class, byXpath(XPathConstants.JLIST));
        jList.click();

        // Use RemoteRobot's Keyboard to go to the top
        Keyboard keyboard = new Keyboard(remoteRobot);
        keyboard.key(KeyEvent.VK_HOME);

        boolean itemFound = false;
        int attempts = 0;

        while (!itemFound && attempts < 30) { // Limit attempts to prevent infinite loop
            try {
                jList.clickItem(type, false);
                itemFound = true;
            } catch (WaitForConditionTimeoutException e) {
                // Scroll down by pressing the Down key
                keyboard.key(KeyEvent.VK_DOWN);
            }
            attempts++;
        }

        if (!itemFound) {
            throw new RuntimeException("Component type not found: " + type);
        }
    }

    public void setStartDevMode(boolean start) {
        JCheckboxFixture checkBox = find(JCheckboxFixture.class, byXpath(XPathConstants.JCHECKBOX));
        if (start) {
            if (!checkBox.isSelected()) {
                checkBox.select();
            }
        } else {
            if (checkBox.isSelected()) {
                checkBox.select();
            }
        }
    }

    public void clickPrevious() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_PREVIOUS)).click();
    }

    public void clickNext() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_NEXT)).click();
    }

    public void clickCreate() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CREATE)).click();
    }

    public void clickCancel() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CANCEL)).click();
    }

    public void clickHelp() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_HELP)).click();
    }
}
