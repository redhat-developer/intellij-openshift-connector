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
import org.jboss.tools.intellij.openshift.test.ui.dialogs.service.CreateNewServiceDialog;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateServiceTest extends AbstractClusterTest {

    private static final String SERVICE_NAME = "test-service";
    private static final String PROJECT_NAME = "newtestproject";

    @Test
    @Order(1)
    @Disabled
    public void createServiceTest() {
        CreateNewServiceDialog createNewServiceDialog = CreateNewServiceDialog.open(robot);
        assertNotNull(createNewServiceDialog);
        createNewServiceDialog.setServiceName(SERVICE_NAME);

        try {
            createNewServiceDialog.selectType(1);
            createNewServiceDialog.setEnvName(SERVICE_NAME);
            createNewServiceDialog.clickOk();
        } catch (WaitForConditionTimeoutException e) {
            // Handle exception: selectType failed
            createNewServiceDialog.selectTemplateByText("EDB Postgres for Kubernetes");
            createNewServiceDialog.clickOk();
        }

        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();

        ProjectClusterTest.verifyProjectHasItem(PROJECT_NAME, SERVICE_NAME);
    }
}
