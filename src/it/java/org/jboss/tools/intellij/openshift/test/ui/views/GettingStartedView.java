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
package org.jboss.tools.intellij.openshift.test.ui.views;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import com.intellij.remoterobot.search.locators.Locator;
import com.intellij.remoterobot.utils.Keyboard;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowPane;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

/**
 * 
 * @author Richard Kocian
 *
 */
@DefaultXpath(by = "Getting Started type", xpath = "//div[@class='IdeFrameImpl']")
@FixtureName(name = "GettingStarted View")
public class GettingStartedView extends ContainerFixture {

  private static final Logger LOGGER = LoggerFactory.getLogger(GettingStartedView.class);
  private final Locator editorPaneLocator = byXpath("//div[@class='JEditorPane']");
  private final Locator backToMainViewLocator = byXpath("//div[@accessiblename='<< Getting Started with OpenShift Toolkit' and @class='JLabel' and @text='<< Getting Started with OpenShift Toolkit']");
  public GettingStartedView(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public void openView() {
    LOGGER.info("Getting Started view: Trying to open Getting Started view");
    final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
    try {
      toolWindowPane.find(ComponentFixture.class, byXpath("//div[@accessiblename='Getting Started' and @class='BaseLabel' and @text='Getting Started']"));
      // If this BaseLabel is found, Getting Started view is already opened
    } catch (Exception e) {
      toolWindowPane.button(byXpath("//div[@tooltiptext='Getting Started']"), Duration.ofSeconds(2)).click();
      LOGGER.info("Getting Started view: Successfully opened");
    }
  }

  public void closeView() {
    LOGGER.info("Getting Started view: Trying to close Getting Started view");
    final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
    try {
      toolWindowPane.find(ComponentFixture.class, byXpath("//div[@accessiblename='Getting Started' and @class='BaseLabel' and @text='Getting Started']"));
      toolWindowPane.button(byXpath("//div[@tooltiptext='Getting Started']"), Duration.ofSeconds(2)).click();
      LOGGER.info("Getting Started view: Successfully closed");
    } catch (Exception e) {
      LOGGER.info("Getting Started view: View is not opened, nothing to do");
    }
  }

  public void waitForTreeItem(String itemText, int duration, int interval) {
    waitFor(Duration.ofSeconds(duration),
        Duration.ofSeconds(interval),
        "The " + itemText + " project is still not available.",
        () -> getGettingStartedTree().hasText(itemText));
  }

  public JTreeFixture getGettingStartedTree(){
    return find(JTreeFixture.class, byXpath("//div[contains(@visible_text, 'all')]"), Duration.ofSeconds(30));
  }

  private boolean isStripeButtonAvailable(ToolWindowPane toolWindowPane, String label) {
    try {
      toolWindowPane.stripeButton(label, false);
    } catch (WaitForConditionTimeoutException e) {
      return false;
    }
    return true;
  }

  public ComponentFixture findEditorPaneFixture() {
    return getRemoteRobot().findAll(GettingStartedView.class, editorPaneLocator).get(0);
  }

  public ComponentFixture findBackToMainButton() {
    return getRemoteRobot().find(GettingStartedView.class, backToMainViewLocator);
  }

  public void maximalizeToolWindow(RemoteRobot robot, Locator toolWindowXPath) {
    robot.find(ComponentFixture.class,toolWindowXPath).click();
    Keyboard keyboard = new Keyboard(robot);
    // Other OS may have different keyboard shortcut for maximizing
    keyboard.hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE);
  }

}
