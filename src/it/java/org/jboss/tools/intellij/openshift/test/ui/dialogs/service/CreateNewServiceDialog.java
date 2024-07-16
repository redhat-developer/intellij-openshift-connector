package org.jboss.tools.intellij.openshift.test.ui.dialogs.service;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.ProjectStructureDialog;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Create Service dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.MYDIALOG_CLASS)
@FixtureName(name = "Create Service Dialog")
public class CreateNewServiceDialog extends CommonContainerFixture {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateNewServiceDialog.class);


    public CreateNewServiceDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public static CreateNewServiceDialog open(RemoteRobot robot) {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        try {
            view.expandOpenshiftExceptDevfile();
        } catch (Exception e) {
            LOGGER.info("Exception while expanding OpenShift view, ignored.", e);
        }
        view.menuRightClickAndSelect(robot, 1, LabelConstants.NEW_SERVICE);
        return robot.find(CreateNewServiceDialog.class, Duration.ofSeconds(60));
    }

    public void selectTemplateByText(String visibleText) {
        ComboBoxFixture typeComboBox = findAll(ComboBoxFixture.class, byXpath("//div[@class='JComboBox']")).get(0);
        typeComboBox.click();
        JListFixture jListFixture = find(JListFixture.class, byXpath(XPathConstants.JLIST));
        jListFixture.clickItem(visibleText, false);
    }

    public void selectType(int index) {
        ComboBoxFixture typeComboBox = findAll(ComboBoxFixture.class, byXpath("//div[@class='JComboBox']")).get(1);
        typeComboBox.click();
        JListFixture jListFixture = find(JListFixture.class, byXpath(XPathConstants.JLIST));
        jListFixture.clickItemAtIndex(index);
    }

    public void selectTypeByText(String visibleText) {
        ComboBoxFixture typeComboBox = findAll(ComboBoxFixture.class, byXpath("//div[@class='JComboBox']")).get(1);
        typeComboBox.click();
        JListFixture jListFixture = find(JListFixture.class, byXpath(XPathConstants.JLIST));
        jListFixture.clickItem(visibleText, false);
    }

    public void setServiceName(String name) {
        List<JTextFieldFixture> textFields = findAll(JTextFieldFixture.class, byXpath(XPathConstants.JTEXT_FIELD));
        JTextFieldFixture nameField = textFields.get(0);  // First text field for service name
        nameField.click();
        nameField.setText(name);
    }

    public void setEnvName(String envName) {
        List<JTextFieldFixture> textFields = findAll(JTextFieldFixture.class, byXpath(XPathConstants.JTEXT_FIELD));
        JTextFieldFixture envNameField = textFields.get(1);  // Second text field for Env Name
        envNameField.click();
        envNameField.setText(envName);
    }

    public void setProvider(String provider) {
        List<JTextFieldFixture> textFields = findAll(JTextFieldFixture.class, byXpath(XPathConstants.JTEXT_FIELD));
        JTextFieldFixture providerField = textFields.get(2);
        providerField.click();
        providerField.setText(provider);
    }

    public void setType(String type) {
        List<JTextFieldFixture> textFields = findAll(JTextFieldFixture.class, byXpath(XPathConstants.JTEXT_FIELD));
        JTextFieldFixture providerField = textFields.get(3);
        providerField.click();
        providerField.setText(type);
    }

    public void clickOk() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_OK)).click();
    }

    public void clickCancel() {
        find(ComponentFixture.class, byXpath("//div[@text.key='button.cancel']")).click();
    }
}
