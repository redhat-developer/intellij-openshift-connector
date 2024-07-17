package org.jboss.tools.intellij.openshift.test.ui.dialogs.cluster_project;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Create New Project dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.MYDIALOG_CLASS)
@FixtureName(name = "Create New Project Dialog")
public class CreateNewProjectDialog extends CommonContainerFixture {

    public CreateNewProjectDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void enterProjectName(String projectName) {
        JTextFieldFixture projectNameField = find(JTextFieldFixture.class, byXpath(XPathConstants.JBTEXTFIELD));
        projectNameField.click();
        projectNameField.setText(projectName);
    }

    public void clickCancel() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CANCEL)).click();
    }

    public void clickCreate() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CREATE)).click();
    }
}
