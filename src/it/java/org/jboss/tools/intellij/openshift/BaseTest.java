/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift;

import com.intellij.openapi.project.Project;
import com.intellij.remoterobot.RemoteRobot;

import com.redhat.devtools.intellij.commonuitest.UITestRunner;

import org.jboss.tools.intellij.openshift.utils.common.CreateComponentFixture;
import org.jboss.tools.intellij.openshift.utils.common.ImportProjectFromVersionControlFixture;
import org.jboss.tools.intellij.openshift.utils.common.ProjectTreeFixture;
import org.jboss.tools.intellij.openshift.utils.common.WelcomeDialogFixture;
import org.jboss.tools.intellij.openshift.utils.openshift.OpenshiftConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class BaseTest {
    private static RemoteRobot robot;

    protected Project project;

    @BeforeAll
    public static void connect() {
        robot = UITestRunner.runIde(UITestRunner.IdeaVersion.ULTIMATE_V_2020_3, 8580);
    }

    @Test
    public void openshiftConnectorTest() throws InterruptedException {
        importProject();
        openOpenshiftConnectorTab();
        deploySampleToCluster();
    }

    @AfterAll
    public static void closeIde() {
        UITestRunner.closeIde();
    }

    private void importProject(){
        final WelcomeDialogFixture welcomeDialogFixture = robot.find(WelcomeDialogFixture.class);
        welcomeDialogFixture.clickToVcsButton();

        final ImportProjectFromVersionControlFixture importProjectFromVersionControlFixture = robot.find(ImportProjectFromVersionControlFixture.class);
        importProjectFromVersionControlFixture.typeUrl();
        importProjectFromVersionControlFixture.clickCloneButton();

        final ProjectTreeFixture projectTreeFixture = robot.find(ProjectTreeFixture.class, Duration.ofSeconds(120));
        projectTreeFixture.waitItemInTree("nodejs-hello-world", 300, 10);
    }

    private void openOpenshiftConnectorTab() throws InterruptedException {
        final OpenshiftConnector openshiftConnector = robot.find(OpenshiftConnector.class, Duration.ofSeconds(60));
        openshiftConnector.clickToOpenshiftButton();
        openshiftConnector.waitItemInTree("https://api.crc.testing:6443/", 300, 5);
    }

    private void deploySampleToCluster() throws InterruptedException {
        final OpenshiftConnector openshiftConnector = robot.find(OpenshiftConnector.class, Duration.ofSeconds(60));
        openshiftConnector.expandOpenshiftConnectorTree();
        openshiftConnector.clickToCreateDeploymentLink();

        final CreateComponentFixture createComponentFixture = robot.find(CreateComponentFixture.class, Duration.ofSeconds(60));
        createComponentFixture.clickToFinishButton();
        openshiftConnector.waitItemInTree("nodejs-hello-world ◉ pushed", 300, 5);
    }

}
