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

import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Ihor Okhrimenko, Ondrej Dockal
 * Test class verifying presence of tested extensions, OpenShift view and it's content
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OpenshiftExtensionTest extends AbstractPublicTest {

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
        view.waitForTreeItem(DEFAULT_CLUSTER_URL, 60, 1);
        view.closeView();
    }

    @Test
    @Order(3)
    public void defaultNodeTest() {
        LOGGER.info("defaultNodeTest: Start");
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        view.waitForTreeItem(DEFAULT_CLUSTER_URL,120,5);
        sleep(3000);
        view.expandOpenshiftViewTree(DEFAULT_CLUSTER_URL);
        view.waitForTreeItem(LabelConstants.PLEASE_LOG_IN_TO_CLUSTER, 120, 5);

        view.closeView();
        LOGGER.info("D" +
                "defaultNodeTest: End");

    }
}
