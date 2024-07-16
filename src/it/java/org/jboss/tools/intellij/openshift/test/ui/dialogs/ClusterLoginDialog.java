/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Martin Szuc
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.dialogs;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants.*;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.*;


/**
 * Cluster login dialog fixture
 *
 * @author mszuc@redhat.com
 */
@DefaultXpath(by = "MyDialog type", xpath = MYDIALOG_CLASS)
@FixtureName(name = "Cluster Login Dialog")
public class ClusterLoginDialog extends CommonContainerFixture {

    public ClusterLoginDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public static ClusterLoginDialog open(RemoteRobot robot) {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.waitForTreeItem(DEVFILE_REGISTRIES, 120, 1);
        view.menuRightClickAndSelect(robot, 0, LOG_IN_TO_CLUSTER);
        return robot.find(ClusterLoginDialog.class, Duration.ofSeconds(20));
    }

    public void close() {
        findText(CANCEL).click();
    }

    public void insertURL(String clusterURL) {
        JTextFieldFixture urlField = findAll(JTextFieldFixture.class, byXpath(JTEXT_FIELD)).get(0);
        urlField.click();
        urlField.setText(clusterURL);
    }

    public void insertToken(String clusterToken) {
        List<JTextFieldFixture> passwordFields = findAll(JTextFieldFixture.class, byXpath(JPASSWORD_FIELD));
        JTextFieldFixture tokenField = passwordFields.get(0);
        tokenField.click();
        tokenField.setText(clusterToken);
    }

    public void insertUsername(String username) {
        JTextFieldFixture usernameField = findAll(JTextFieldFixture.class, byXpath(JTEXT_FIELD)).get(1);
        usernameField.click();
        usernameField.setText(username);
    }

    public void insertPassword(String password) {
        List<JTextFieldFixture> passwordFields = findAll(JTextFieldFixture.class, byXpath(JPASSWORD_FIELD));
        JTextFieldFixture passwordField = passwordFields.get(1);
        passwordField.click();
        passwordField.setText(password);
    }

    public String getURL() {
        JTextFieldFixture urlField = findAll(JTextFieldFixture.class, byXpath(JTEXT_FIELD)).get(0);
        return urlField.getText();
    }

    public String getToken() {
        List<JTextFieldFixture> passwordFields = findAll(JTextFieldFixture.class, byXpath(JPASSWORD_FIELD));
        JTextFieldFixture tokenField = passwordFields.get(0);
        return tokenField.getText();
    }

    public String getUsername() {
        JTextFieldFixture usernameField = findAll(JTextFieldFixture.class, byXpath(JTEXT_FIELD)).get(1);
        return usernameField.getText();
    }

    public String getPassword() {
        List<JTextFieldFixture> passwordFields = findAll(JTextFieldFixture.class, byXpath(JPASSWORD_FIELD));
        JTextFieldFixture passwordField = passwordFields.get(1);
        return passwordField.getText();
    }

    public void clickPasteLoginCommand() {
        find(ComponentFixture.class, byXpath(BUTTON_PASTE_LOGIN_COMMAND)).click();
    }


}