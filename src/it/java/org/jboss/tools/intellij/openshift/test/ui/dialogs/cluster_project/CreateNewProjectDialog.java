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
import com.intellij.remoterobot.fixtures.JTextFieldFixture;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Create New Project dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.MYDIALOG_CLASS)
@FixtureName(name = "Create New Project Dialog")
public class CreateNewProjectDialog extends CommonContainerFixture {

    public CreateNewProjectDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void enterProjectName(String projectName) {
        JTextFieldFixture projectNameField = find(JTextFieldFixture.class, byXpath(XPathConstants.JBTEXTFIELD));
        projectNameField.click();
        projectNameField.setText(projectName);
    }

    public void clickCreate() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CREATE)).click();
    }
}
