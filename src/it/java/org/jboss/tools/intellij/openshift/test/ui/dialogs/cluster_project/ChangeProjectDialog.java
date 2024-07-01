package org.jboss.tools.intellij.openshift.test.ui.dialogs.cluster_project;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.CommonContainerFixture;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.utils.Keyboard;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Change Active Project dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.CHANGE_CLUSTER_PROJECT_DIALOG)
@FixtureName(name = "Change Active Project Dialog")
public class ChangeProjectDialog extends CommonContainerFixture {

    public ChangeProjectDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void enterProjectName(RemoteRobot remoteRobot, String projectName) {
        // Focus the text field
        find(ComponentFixture.class, byXpath(XPathConstants.TEXT_FIELD_W_AUTO_COMPLETION)).click();

        // Use RemoteRobot's Keyboard to type the project name
        Keyboard keyboard = new Keyboard(remoteRobot);
        keyboard.enterText(projectName);
    }

    public void clickChange() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CHANGE)).click();
    }

    public void clickCancel() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CANCEL)).click();
    }
}