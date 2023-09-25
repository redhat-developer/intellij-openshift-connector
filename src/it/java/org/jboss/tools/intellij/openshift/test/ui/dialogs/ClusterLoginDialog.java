/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.dialogs;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jboss.tools.intellij.openshift.test.ui.steps.SharedSteps;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;


/**
 * Cluster login dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = "//div[@accessiblename='Cluster login' and @class='MyDialog']")
@FixtureName(name = "Cluster Login Dialog")
public class ClusterLoginDialog extends CommonContainerFixture {
    private static SharedSteps sharedSteps = new SharedSteps();
    private static boolean isShowing = false;

    public ClusterLoginDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public static ClusterLoginDialog open(RemoteRobot robot) {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.waitForTreeItem("Devfile registries",20,1);
        view.menuRightClickAndSelect(robot,0, "Log in to cluster");
        return robot.find(ClusterLoginDialog.class, Duration.ofSeconds(20));
    }
    public void close(RemoteRobot robot){
        robot.find(ClusterLoginDialog.class,Duration.ofSeconds(20)).findText("Cancel").click();
    }
    public void insertURL(RemoteRobot robot, String clusterURL){
        JTextFieldFixture urlField = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JTextField']")).get(0);
        urlField.click();
        urlField.setText(clusterURL);
    }

    public void insertToken(RemoteRobot robot, String clusterToken) {
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture tokenField = passwordFields.get(0);
        tokenField.click();
        tokenField.setText(clusterToken);
    }
    public void insertUsername(RemoteRobot robot, String username){
        JTextFieldFixture usernameField = robot.find(JTextFieldFixture.class, byXpath("//div[@text='Username:']/following-sibling::div[@class='JTextField']"));
        usernameField.click();
        usernameField.setText(username);
    }
    public void insertPassword(RemoteRobot robot, String password){
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture passwordField = passwordFields.get(1);
        passwordField.click();
        passwordField.setText(password);
    }

}
