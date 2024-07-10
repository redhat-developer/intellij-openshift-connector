package org.jboss.tools.intellij.openshift.test.ui.dialogs.cluster_project;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Delete Project dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.MYDIALOG_CLASS)
@FixtureName(name = "Delete Project Dialog")
public class DeleteProjectDialog extends CommonContainerFixture {

    public DeleteProjectDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void clickYes() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_YES)).click();
    }

    public void clickNo() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_NO)).click();
    }
}