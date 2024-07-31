package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.dataExtractor.RemoteText;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComponentNodeTest extends AbstractClusterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentNodeTest.class);

    @Test
    @Order(1)
    public void openCloseCreateComponentDialogTest() {
        // Open the Create Component dialog
        CreateComponentDialog createComponentDialog = CreateComponentDialog.open(robot);
        assertNotNull(createComponentDialog);

        createComponentDialog.close();
        assertThrowsExactly(WaitForConditionTimeoutException.class, () -> {
            robot.find(CreateComponentDialog.class, Duration.ofSeconds(2));
        });
    }

    @Test
    @Order(2)
    public void createComponentTest() {
        String COMPONENT_NAME = "test-component";

        CreateComponentDialog createComponentDialog = CreateComponentDialog.open(robot);
        assertNotNull(createComponentDialog);
        createComponentDialog.setName(COMPONENT_NAME);
        createComponentDialog.selectComponentType("Node.js Runtime", robot);
        createComponentDialog.setStartDevMode(true);
        createComponentDialog.selectProjectStarter("nodejs-starter");
        createComponentDialog.clickCreate();

        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();

        ProjectClusterTest.verifyProjectHasItem("newtestproject", COMPONENT_NAME);
    }

    @Test
    @Order(3)
    public void startDevModeOnClusterComponentTest() {
        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.openView();
        openshiftView.expandOpenshiftExceptDevfile();
        openshiftView.menuRightClickAndSelect(robot, 2, "Start dev on Cluster");

        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();
        assertDevModeStarted();
    }

    @Test
    @Order(4)
    public void stopDevModeOnClusterComponentTest() {
        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.openView();
        openshiftView.expandOpenshiftExceptDevfile();
        openshiftView.menuRightClickAndSelect(robot, 2, "Stop dev on Cluster");

        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();
        assertDevModeStopped();
    }

    private void assertDevModeStarted() {
        assertTextInOpenshiftTree(Duration.ofSeconds(240), Duration.ofSeconds(10), "debug", "dev");

        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.expandOpenshiftExceptDevfile();
        openshiftView.waitForTreeItem("runtime (3000)", 240, 10);
    }

    private void assertDevModeStopped() {
        assertTextInOpenshiftTree(Duration.ofSeconds(240), Duration.ofSeconds(10), "locally created");
    }

    private void assertTextInOpenshiftTree(Duration timeout, Duration interval, String... texts) {
        ComponentFixture openShiftTree = robot.find(OpenshiftView.class, Duration.ofSeconds(2)).getOpenshiftConnectorTree();

        waitFor(
                timeout,
                interval,
                "text: " + String.join(", ", texts),
                "Expected texts not found: " + String.join(", ", texts),
                () -> {
                    List<String> renderedText = openShiftTree.findAllText()
                            .stream()
                            .map(RemoteText::getText)
                            .map(String::trim)
                            .toList();
                    for (String text : texts) {
                        boolean found = renderedText.stream().anyMatch(t -> t.contains(text.trim()));
                        if (!found) {
                            return false;
                        }
                    }
                    return true;
                }
        );
    }
}
