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
import com.intellij.remoterobot.fixtures.ContainerFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.fixtures.JTreeFixture;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowPane;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

/**
 * 
 * @author Ihor Okhrimenko, Ondrej Dockal
 *
 */
@DefaultXpath(by = "OpenshiftView type", xpath = "//div[@class='IdeFrameImpl']")
@FixtureName(name = "Openshift View")
public class OpenshiftView extends ContainerFixture {
  public OpenshiftView(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public void openView() {
    final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
    waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'OpenShift' stripe button is not available.", () -> isStripeButtonAvailable(toolWindowPane, "OpenShift"));
    toolWindowPane.stripeButton("OpenShift", false).click();
  }

  public void closeView() {
    final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
    toolWindowPane.button(byXpath("//div[@tooltiptext='OpenShift']"), Duration.ofSeconds(2)).click();
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

  public JTreeFixture getOpenshiftConnectorTree(){
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

}
