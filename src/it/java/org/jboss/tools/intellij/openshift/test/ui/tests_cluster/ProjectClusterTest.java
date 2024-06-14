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

import com.intellij.remoterobot.fixtures.dataExtractor.RemoteText;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.cluster_project.ChangeProjectDialog;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.cluster_project.CreateNewProjectDialog;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.cluster_project.DeleteProjectDialog;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.fest.assertions.Fail.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectClusterTest extends AbstractClusterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectClusterTest.class);
    private static final String PROJECT_NAME = "testproject";
    private static final String NEW_PROJECT_NAME = "newtestproject";

    @Test
    @Order(1)
    public void createNewProjectTest() {
        LOGGER.info("createNewProjectTest: Start");
        OpenshiftView openshiftView = robot.find(OpenshiftView.class);
        openshiftView.openView();
        openshiftView.menuRightClickAndSelect(robot, 0, LabelConstants.NEW_PROJECT);

        createNewClusterProject(PROJECT_NAME);
        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();

        verifyProjectIsVisible(PROJECT_NAME);
        LOGGER.info("createNewProjectTest: End");
    }

    @Test
    @Order(2)
    public void changeActiveProjectTest() {
        LOGGER.info("changeActiveProjectTest: Start");
        OpenshiftView openshiftView = robot.find(OpenshiftView.class);
        openshiftView.openView();
        openshiftView.menuRightClickAndSelect(robot, 0, LabelConstants.NEW_PROJECT);

        createNewClusterProject(NEW_PROJECT_NAME);
        verifyProjectIsVisible(NEW_PROJECT_NAME);

        openshiftView.menuRightClickAndSelect(robot, 0, LabelConstants.CHANGE_PROJECT);

        ChangeProjectDialog changeProjectDialog = robot.find(ChangeProjectDialog.class, Duration.ofSeconds(20));
        changeProjectDialog.enterProjectName(robot, PROJECT_NAME);
        changeProjectDialog.clickChange();


        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();
        verifyProjectIsVisible(PROJECT_NAME);
        LOGGER.info("changeActiveProjectTest: End");
    }

    @Test
    @Order(3)
    public void deleteProjectTest() {
        LOGGER.info("deleteProjectTest: Start");
        OpenshiftView openshiftView = robot.find(OpenshiftView.class);
        openshiftView.openView();
        openshiftView.menuRightClickAndSelect(robot, 1, LabelConstants.DELETE_PROJECT);

        DeleteProjectDialog deleteProjectDialog = robot.find(DeleteProjectDialog.class, Duration.ofSeconds(20));
        deleteProjectDialog.clickYes();

        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();

        verifyProjectHasItem(PROJECT_NAME, "Missing project,");
        LOGGER.info("deleteProjectTest: End");
    }

    private static void createNewClusterProject(String projectName) {
        CreateNewProjectDialog createNewProjectDialog = robot.find(CreateNewProjectDialog.class, Duration.ofSeconds(20));
        createNewProjectDialog.enterProjectName(projectName);
        createNewProjectDialog.clickCreate();
    }

    private void verifyProjectIsVisible(String projectName) {
        LOGGER.info("Verifying project creation for: " + projectName);
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.refreshTree(robot);
        sleep(3000);
        view.expandOpenshiftExceptDevfile();
        view.waitForTreeItem(projectName, 120, 5);
        LOGGER.info("Project " + projectName + " is created and visible in the OpenShift view.");
    }

    private void verifyProjectHasItem(String projectName, String itemName) {
        LOGGER.info("Verifying project " + projectName + " has item: " + itemName);
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.refreshTree(robot);
        view.waitForTreeItem(projectName, 20, 5);
        sleep(3000);
        view.expandOpenshiftExceptDevfile();

        boolean containsItem = view.getOpenshiftConnectorTree().findAllText().stream()
                .map(RemoteText::getText)
                .anyMatch(text -> text.contains(itemName));

        if (containsItem) {
            LOGGER.info("Project " + projectName + " has item: " + itemName);
        } else {
            LOGGER.error("Project " + projectName + " does not have item: " + itemName);
            fail("Project " + projectName + " does not have item: " + itemName);
        }
    }

}