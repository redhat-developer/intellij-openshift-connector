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

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.FlatWelcomeFrame;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowPane;
import com.redhat.devtools.intellij.commonuitest.utils.project.CreateCloseUtils;
import org.jboss.tools.intellij.openshift.test.ui.annotations.UITest;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.ProjectStructureDialog;
import org.jboss.tools.intellij.openshift.test.ui.junit.TestRunnerExtension;
import org.jboss.tools.intellij.openshift.test.ui.runner.IdeaRunner;
import org.jboss.tools.intellij.openshift.test.ui.utils.CleanUpUtility;
import org.jboss.tools.intellij.openshift.test.ui.utils.ProjectUtility;
import org.jboss.tools.intellij.openshift.test.ui.utils.KubeConfigUtility;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.GettingStartedView;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author Ondrej Dockal, odockal@redhat.com
 */
@ExtendWith(TestRunnerExtension.class)
@UITest
public abstract class AbstractBaseTest {
    protected static RemoteRobot robot;
    private static boolean isProjectCreatedAndOpened = false;
    public static final String DEFAULT_CLUSTER_URL = "no (current) context/cluster set";
    protected static String currentClusterUrl = DEFAULT_CLUSTER_URL;

    @BeforeAll
    public static void setUpProject() {

        robot = IdeaRunner.getInstance().getRemoteRobot();

        if (!isProjectCreatedAndOpened) {
            FlatWelcomeFrame flatWelcomeFrame = robot.find(FlatWelcomeFrame.class, Duration.ofSeconds(10));
            flatWelcomeFrame.disableNotifications();
            flatWelcomeFrame.preventTipDialogFromOpening();

            CreateCloseUtils.createNewProject(robot, "test-project", CreateCloseUtils.NewProjectType.PLAIN_JAVA);
            ProjectStructureDialog.cancelProjectStructureDialogIfItAppears(robot);
            ProjectUtility.closeGotItPopup(robot);

            IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
            ideStatusBar.waitUntilAllBgTasksFinish();

            isProjectCreatedAndOpened = true;
        }
    }

    @AfterEach
    public void afterEachCleanUp() {
        CleanUpUtility.cleanUpAll(robot);
    }

    protected static void logOut() {
        KubeConfigUtility.removeKubeConfig();
        sleep(2000);
        currentClusterUrl = DEFAULT_CLUSTER_URL;

        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.waitForTreeItem(LabelConstants.DEVFILE_REGISTRIES,120,5); // Wait for "loading..." to finish

        view.refreshTree(robot);
        IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class);
        ideStatusBar.waitUntilAllBgTasksFinish();

        view.closeView();
    }

    public RemoteRobot getRobotReference() {
        return robot;
    }

    public boolean isStripeButtonAvailable(String label) {
        try {
            ToolWindowPane toolWindowPane = robot.find(ToolWindowPane.class);
            toolWindowPane.stripeButton(label, false);
        } catch (WaitForConditionTimeoutException e) {
            return false;
        }
        return true;
    }

    protected static void sleep(long ms) {
        System.out.println("Putting thread into sleep for: " + ms + " ms");
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
