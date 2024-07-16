package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.service.CreateNewServiceDialog;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateServiceTest extends AbstractClusterTest {

    private static final String SERVICE_NAME = "test-service";
    private static final String ENV_NAME = "env-name";
    private static final String PROVIDER_NAME = "test-provider";
    private static final String PROJECT_NAME = "newtestproject";

    @Test
    @Order(1)
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
