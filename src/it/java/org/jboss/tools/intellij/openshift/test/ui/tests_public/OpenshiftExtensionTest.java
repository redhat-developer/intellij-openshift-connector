/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Richard Kocian, Martin Szuc
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.tests_public;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.redhat.devtools.intellij.commonuitest.utils.constants.XPathDefinitions;
import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.GettingStartedView;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ihor Okhrimenko, Ondrej Dockal
 * Test class verifying presence of tested extensions, OpenShift view and it's content
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OpenshiftExtensionTest extends AbstractBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenshiftExtensionTest.class);

    @BeforeAll
    public static void logoutOnceBeforeAllTests() {
        logOut();
    }

    @Test
    @Order(1)
    public void openshiftExtensionTest() {
        waitFor(Duration.ofSeconds(10),
                Duration.ofSeconds(1),
                "The 'OpenShift' stripe button is not available.",
                () -> isStripeButtonAvailable(OPENSHIFT));

        waitFor(Duration.ofSeconds(10),
                Duration.ofSeconds(1),
                "The 'Kubernetes' stripe button is not available.",
                () -> isStripeButtonAvailable(KUBERNETES));

        waitFor(Duration.ofSeconds(10),
                Duration.ofSeconds(1),
                "The 'Getting Started' stripe button is not available.",
                () -> isStripeButtonAvailable(GETTING_STARTED));
    }

    @Test
    @Order(2)
    public void openshiftViewTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.waitForTreeItem(DEFAULT_CLUSTER_URL, 10, 1);
        view.closeView();
    }

    @Test
    @Order(3)
    public void defaultNodeTest() {
        LOGGER.info("defaultNodeTest: Start");
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        view.waitForTreeItem(DEFAULT_CLUSTER_URL, 120, 5);
        view.getOpenshiftConnectorTree().doubleClickRow(0);
        view.waitForTreeItem(LabelConstants.PLEASE_LOG_IN_TO_CLUSTER, 120, 5);

        view.closeView();
        LOGGER.info("D" +
                "defaultNodeTest: End");

    }
    @Test
    @Order(4)
    public void openGettingStartedFromOpenshiftView() {
        OpenshiftView openshiftView = robot.find(OpenshiftView.class);
        openshiftView.openView();

        openshiftView.getOpenshiftConnectorTree().rightClickRow(0);
        robot.find(ComponentFixture.class, byXpath(XPathConstants.GETTING_STARTED_ACTION_MENU_ITEM), Duration.ofSeconds(2)).click();

        GettingStartedView gettingStartedView = robot.find(GettingStartedView.class);
        assertTrue(gettingStartedView.isShowing(), "Getting Started view is not showing");

        gettingStartedView.closeView();
        openshiftView.closeView();
    }
}
