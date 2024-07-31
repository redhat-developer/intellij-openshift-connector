package org.jboss.tools.intellij.openshift.test.ui.common;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.search.locators.Locator;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;
import static com.redhat.devtools.intellij.commonuitest.utils.steps.SharedSteps.waitForComponentByXpath;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.JB_TERMINAL_PANEL;

/**
 * Fixture for interacting with the terminal panel in the IDE.
 */
@DefaultXpath(by = "TerminalPanelFixture type", xpath = XPathConstants.JB_TERMINAL_PANEL)
@FixtureName(name = "Terminal Panel")
public class TerminalPanelFixture extends ComponentFixture {

    public TerminalPanelFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void rightClickSelect(RemoteRobot robot, Locator xpath) {
        Point linkPosition = new Point(20, 20);
        ComponentFixture terminalPanel = robot.find(ComponentFixture.class, byXpath(JB_TERMINAL_PANEL));
        terminalPanel.rightClick(linkPosition);
        waitForComponentByXpath(robot, 20, 1, xpath);
        robot.find(ComponentFixture.class, xpath)
                .click();
    }
}