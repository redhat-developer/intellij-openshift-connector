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
package org.jboss.tools.intellij.openshift.test.ui;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.search.locators.Locator;
import org.jboss.tools.intellij.openshift.test.ui.views.GettingStartedView;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ihor Okhrimenko, Ondrej Dockal, Richard Kocian
 * Base UI test class verifying presence of tested extensions and OpenShift View presence and content
 */
public class BaseUITest extends AbstractBaseTest {

	@Test
	public void openshiftExtensionTest() {
		waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'OpenShift' stripe button is not available.", () -> isStripeButtonAvailable("OpenShift"));
		waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'Kubernetes' stripe button is not available.", () -> isStripeButtonAvailable("Kubernetes"));
		waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'Getting Started' stripe button is not available.", () -> isStripeButtonAvailable("Getting Started"));
	}

	@Test
	public void openshiftViewTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
		view.openView();
		view.waitForTreeItem("https://kubernetes.default.svc/", 10, 1);
		view.waitForTreeItem("Devfile registries", 10, 1);
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

		Locator next = byXpath("//div[@accessiblename='Next: Browse all the Devfile stacks from Devfile registries >' and @class='JLabel' and @text='Next: Browse all the Devfile stacks from Devfile registries >']");
		ComponentFixture nextFixture = robot.find(GettingStartedView.class, next);
		nextFixture.click();
		assertFalse(view.findEditorPaneFixture().findAllText().isEmpty(), "Browse all the Devfile stacks from Devfile registries item has empty description!");

		view.findBackToMainButton().click();

		view.getGettingStartedTree().findText("Debug the component").click();
		assertFalse(view.findEditorPaneFixture().findAllText().isEmpty(), "Debug the component item has empty description!");

		Locator back = byXpath("//div[@accessiblename='< Back: Start a component in development mode' and @class='JLabel' and @text='< Back: Start a component in development mode']");
		ComponentFixture backFixture = robot.find(GettingStartedView.class, back);
		backFixture.click();
		assertFalse(view.findEditorPaneFixture().findAllText().isEmpty(), "Start a component in development mode item has empty description!");

		view.findBackToMainButton().click();

		Locator feedback = byXpath("//div[@text='Leave feedback']");
		ComponentFixture feedbackFixture = robot.find(GettingStartedView.class, feedback);
		assertTrue(feedbackFixture.isShowing(), "Feedback button is not showing");

		view.closeView();
	}


}