/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Martin Szuc, Richard Kocian
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.tests_public;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.search.locators.Locator;
import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.views.GettingStartedView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GettingStartedTest extends AbstractPublicTest {

    @Test
    @BeforeAll
    public static void gettingStartedShowsOnStartup() {
        GettingStartedView view = robot.find(GettingStartedView.class);
        view.closeView();
    }

    @Test
    public void gettingStartedContainsAllItems() {
        GettingStartedView view = robot.find(GettingStartedView.class);
        view.openView();

        view.waitForTreeItem("Login/Provision OpenShift cluster", 10, 1);
        view.waitForTreeItem("Browse all the Devfile stacks from Devfile registries", 10, 1);
        view.waitForTreeItem("Create a Component", 10, 1);
        view.waitForTreeItem("Start a component in development mode", 10, 1);
        view.waitForTreeItem("Debug the component", 10, 1);
        view.waitForTreeItem("Deploy component on the cluster", 10, 1);

        view.closeView();
    }

    @Test
    public void gettingStartedBehaviour() {
        GettingStartedView view = robot.find(GettingStartedView.class);
        view.openView();

        view.maximalizeToolWindow(robot);

        view.getGettingStartedTree().findText("Login/Provision OpenShift cluster").click();
        assertFalse(view.findEditorPaneFixture().findAllText().isEmpty(), "Login/Provision OpenShift cluster item has empty description!");

        Locator next = byXpath(NEXT_BROWSE_ALL_THE_DEVFILE_STACKS);
        ComponentFixture nextFixture = robot.find(GettingStartedView.class, next);
        nextFixture.click();
        assertFalse(view.findEditorPaneFixture().findAllText().isEmpty(), "Browse all the Devfile stacks from Devfile registries item has empty description!");

        view.findBackToMainButton().click();

        view.getGettingStartedTree().findText("Debug the component").click();
        assertFalse(view.findEditorPaneFixture().findAllText().isEmpty(), "Debug the component item has empty description!");

        Locator back = byXpath(BACK_START_A_COMPONENT_IN_DEVELOPMENT_MODE);
        ComponentFixture backFixture = robot.find(GettingStartedView.class, back);
        backFixture.click();
        assertFalse(view.findEditorPaneFixture().findAllText().isEmpty(), "Start a component in development mode item has empty description!");

        view.findBackToMainButton().click();

        Locator feedback = byXpath(LEAVE_FEEDBACK);
        ComponentFixture feedbackFixture = robot.find(GettingStartedView.class, feedback);
        assertTrue(feedbackFixture.isShowing(), "Feedback button is not showing");

        view.closeView();
    }
}
