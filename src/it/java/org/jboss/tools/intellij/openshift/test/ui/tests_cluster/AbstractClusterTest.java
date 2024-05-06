/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Martin Szuc
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.ClusterLoginDialog;

import org.jboss.tools.intellij.openshift.test.ui.utils.CleanUpUtility;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.redhat.devtools.intellij.commonuitest.utils.steps.SharedSteps.waitForComponentByXpath;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants.*;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.*;

/**
 * @author Martin Szuc
 * Abstract test class used for common methods used across tests that require cluster login.
 */
public abstract class AbstractClusterTest extends AbstractBaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClusterTest.class);
    private static final String CLUSTER_URL = System.getenv("CLUSTER_URL");
    private static final String CLUSTER_USER = System.getenv("CLUSTER_USER");
    private static final String CLUSTER_PASSWORD = System.getenv("CLUSTER_PASSWORD");

    @BeforeAll
    public static void setUp(){
        CleanUpUtility.cleanUpAll(robot);
    }

    protected void loginWithUsername() {
        LOGGER.info("Opening cluster login dialog");
        ClusterLoginDialog clusterLoginDialog = ClusterLoginDialog.open(robot);
        clusterLoginDialog.insertURL(CLUSTER_URL);
        clusterLoginDialog.insertUsername(CLUSTER_USER);
        clusterLoginDialog.insertPassword(CLUSTER_PASSWORD);
        clusterLoginDialog.button(OK).click();

        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();
    }

    protected void verifyClusterLogin(String expectedURL) {
        LOGGER.info("Verifying login");
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();


        LOGGER.info("Waiting for '" + expectedURL + "' to appear.");
        view.waitForTreeItem(expectedURL, 120, 5);
        IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class);
        ideStatusBar.waitUntilAllBgTasksFinish();

        try {
            view.expandOpenshiftExceptDevfile();
        } catch (Exception e) {
            view.closeView();
            view.openView();
            try {
                view.expandOpenshiftExceptDevfile();
            } catch (Exception ex) {
                LOGGER.error("Expanding Openshift tree failed!");
            }
        }
        sleep(2000);
        view.refreshTree(robot);
        ideStatusBar.waitUntilAllBgTasksFinish();

        waitForLoginToFinish(view, expectedURL);

        view.getOpenshiftConnectorTree().rightClickRow(0);
        waitForComponentByXpath(robot, 5, 1, byXpath(OPEN_CONSOLE_DASHBOARD));
        LOGGER.info("Login successfully verified");

        view.closeView();
    }

    private static void waitForLoginToFinish(OpenshiftView view, String expectedURL) {
        // Check for "loading…" and wait if necessary
        boolean isLoading = true;
        int attempts = 0;
        while (isLoading && attempts < 24) { // 24 attempts * 5 seconds = 2 minutes max wait
            try {
                LOGGER.info("Waiting for login to finish, attempt #" + attempts);
                view.waitForTreeItem(expectedURL, 5, 1);
                isLoading = false;
            } catch (Exception e) {
                attempts++;
            }
        }
    }

    private void checkUrlFormat() {
        if (!currentClusterUrl.endsWith("/")) {
            currentClusterUrl += "/";
        }
    }
}