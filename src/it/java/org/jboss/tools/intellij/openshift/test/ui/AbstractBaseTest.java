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
import org.jboss.tools.intellij.openshift.test.ui.utils.ProjectUtility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

/**
 * @author Ondrej Dockal, odockal@redhat.com
 */
@ExtendWith(TestRunnerExtension.class)
@UITest
public abstract class AbstractBaseTest {

    protected static RemoteRobot robot;
    private static boolean hasConnectedToTestIDE = false;

    @BeforeAll
    public static void connect() {
        if (!hasConnectedToTestIDE) {
            robot = IdeaRunner.getInstance().getRemoteRobot();
            hasConnectedToTestIDE = true;

            FlatWelcomeFrame flatWelcomeFrame = robot.find(FlatWelcomeFrame.class, Duration.ofSeconds(10));
            flatWelcomeFrame.disableNotifications();
            flatWelcomeFrame.preventTipDialogFromOpening();

            CreateCloseUtils.createNewProject(robot, "test-project", CreateCloseUtils.NewProjectType.PLAIN_JAVA);
            ProjectStructureDialog.cancelProjectStructureDialogIfItAppears(robot);
            ProjectUtility.closeGotItPopup(robot);

            IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
            ideStatusBar.waitUntilAllBgTasksFinish();
        }
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
}
