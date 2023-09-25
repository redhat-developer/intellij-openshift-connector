/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.views;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowPane;
import org.jboss.tools.intellij.openshift.test.ui.steps.SharedSteps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

/**
 * @author Ihor Okhrimenko, Ondrej Dockal
 */
@DefaultXpath(by = "OpenshiftView type", xpath = "//div[@class='IdeFrameImpl']")
@FixtureName(name = "Openshift View")
public class OpenshiftView extends ContainerFixture {

    private final SharedSteps sharedSteps = new SharedSteps();
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenshiftView.class);

    public OpenshiftView(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void openView() {
        LOGGER.info("Openshift view: Trying to open Openshift view");
        final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
        try {
            toolWindowPane.find(ComponentFixture.class, byXpath("//div[@accessiblename='OpenShift' and @class='BaseLabel' and @text='OpenShift']"));
            // If this BaseLabel is found, Openshift view is already opened
        } catch (Exception e) {
            toolWindowPane.button(byXpath("//div[@text='OpenShift']"), Duration.ofSeconds(2)).click();
            LOGGER.info("Openshift view: Successfully opened");
        }
    }

    public void closeView() {
        LOGGER.info("Openshift view: Trying to close Openshift view");
        final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
        try {
            toolWindowPane.find(ComponentFixture.class, byXpath("//div[@accessiblename='OpenShift' and @class='BaseLabel' and @text='OpenShift']"));
            toolWindowPane.button(byXpath("//div[@tooltiptext='OpenShift']"), Duration.ofSeconds(2)).click();
            LOGGER.info("Openshift view: Successfully closed");
        } catch (Exception e) {
            LOGGER.info("Openshift view: View is not opened, nothing to do");
        }
    }

    public void expandOpenshiftViewTree(String path) {
        getOpenshiftConnectorTree().expand(path);
    }

    public void waitForTreeItem(String itemText, int duration, int interval) {
        waitFor(Duration.ofSeconds(duration),
                Duration.ofSeconds(interval),
                "The " + itemText + " project is still not available.",
                () -> getOpenshiftConnectorTree().hasText(itemText));
    }

    public JTreeFixture getOpenshiftConnectorTree() {
        return find(JTreeFixture.class, byXpath("//div[contains(@visible_text, 'Devfile registries')]"), Duration.ofSeconds(30));
    }

    public void menuRightClickAndSelect(RemoteRobot robot, int row, String selection) {
        getOpenshiftConnectorTree().clickRow(row);
        getOpenshiftConnectorTree().rightClickRow(row);
        sharedSteps.waitForComponentByXpath(robot, 20, 1, byXpath("//div[@text='" + selection + "']"));
        robot.find(ComponentFixture.class, byXpath("//div[@text='" + selection + "']")).click();
    }

    private boolean isStripeButtonAvailable(ToolWindowPane toolWindowPane, String label) {
        try {
            toolWindowPane.stripeButton(label, false);
        } catch (WaitForConditionTimeoutException e) {
            return false;
        }
        return true;
    }

}
