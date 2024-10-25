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
package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.component.CreateComponentDialog;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateComponentTest extends AbstractClusterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateComponentTest.class);

    @Test
    @Order(1)
    public void openCloseCreateComponentDialogTest() {
        LOGGER.info("openCloseCreateComponentDialogTest: Start");
        // Open the Create Component dialog
        CreateComponentDialog createComponentDialog = CreateComponentDialog.open(robot);
        assertNotNull(createComponentDialog);

        createComponentDialog.close();
        assertThrowsExactly(WaitForConditionTimeoutException.class, () -> robot.find(CreateComponentDialog.class, Duration.ofSeconds(2)));
        LOGGER.info("openCloseCreateComponentDialogTest: End");
    }

    @Test
    @Order(2)
    public void createGoRuntimeComponentTest() {
        LOGGER.info("createGoRuntimeComponentTest: Start");
        String COMPONENT_NAME = "test-component";

        CreateComponentDialog createComponentDialog = CreateComponentDialog.open(robot);
        assertNotNull(createComponentDialog);
        createComponentDialog.setName(COMPONENT_NAME);
        createComponentDialog.selectComponentType("Go Runtime", robot);
        createComponentDialog.setStartDevMode(true);
        createComponentDialog.clickCreate();

        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();

        ProjectClusterTest.verifyProjectHasItem("newtestproject", COMPONENT_NAME);
        LOGGER.info("createGoRuntimeComponentTest: End");
    }
}
