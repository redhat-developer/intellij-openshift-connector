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

  private final Locator editorPaneLocator = byXpath("//div[@class='JEditorPane']");
  private final Locator backToMainViewLocator = byXpath("//div[@accessiblename='<< Getting Started with OpenShift Toolkit' and @class='JLabel' and @text='<< Getting Started with OpenShift Toolkit']");
  public GettingStartedView(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public void openView() {
    final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
    waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'Getting Started' stripe button is not available.", () -> isStripeButtonAvailable(toolWindowPane, "OpenShift"));
    toolWindowPane.stripeButton("Getting Started", false).click();
  }

  public void closeView() {
    final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
    toolWindowPane.button(byXpath("//div[@tooltiptext='Getting Started']"), Duration.ofSeconds(2)).click();
  }

  public void waitForTreeItem(String itemText, int duration, int interval) {
    waitFor(Duration.ofSeconds(duration),
        Duration.ofSeconds(interval),
        "The " + itemText + " project is still not available.",
        () -> getGettingStartedTree().hasText(itemText));
  }

  public JTreeFixture getGettingStartedTree(){
    return find(JTreeFixture.class, byXpath("//div[@class='Tree']"), Duration.ofSeconds(30));
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

  public void maximalizeToolWindow(RemoteRobot robot) {
    Keyboard keyboard = new Keyboard(robot);
    // Other OS may have different keyboard shortcut for maximizing
    keyboard.hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE);
  }

}
