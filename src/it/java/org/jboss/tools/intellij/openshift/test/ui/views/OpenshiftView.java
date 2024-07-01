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
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowPane;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static com.redhat.devtools.intellij.commonuitest.utils.steps.SharedSteps.waitForComponentByXpath;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants.*;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.*;

/**
 *
 * @author Ihor Okhrimenko, Ondrej Dockal, Martin Szuc
 *
 */
@DefaultXpath(by = "OpenshiftView type", xpath = IDE_FRAME_IMPL)
@FixtureName(name = "Openshift View")
public class OpenshiftView extends ContainerFixture {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenshiftView.class);

    public OpenshiftView(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void openView() {
        if (!isViewOpened()) {
            final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
            toolWindowPane.button(byXpath(getToolWindowButton(OPENSHIFT)), Duration.ofSeconds(2)).click();
            LOGGER.info("Openshift view opened");
        }
    }

    public void closeView() {
        if (isViewOpened()) {
            final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
            toolWindowPane.button(byXpath(getToolWindowButton(OPENSHIFT)), Duration.ofSeconds(2)).click();
            LOGGER.info("Openshift view closed");
        }
    }

    public void expandOpenshiftViewTree(String path) {
        getOpenshiftConnectorTree().expand(path);
    }

    public void expandOpenshiftExceptDevfile() {
        getOpenshiftConnectorTree().expandAllExcept(DEVFILE_REGISTRIES);
    }

    public void waitForTreeItem(String itemText, int duration, int interval) {
        waitFor(Duration.ofSeconds(duration),
                Duration.ofSeconds(interval),
                "The " + itemText + " project is still not available.",
                () -> getOpenshiftConnectorTree().hasText(itemText));
    }

    public JTreeFixture getOpenshiftConnectorTree() {
        return find(JTreeFixture.class, byXpath(TREE_CLASS), Duration.ofSeconds(30));
    }

    public void menuRightClickAndSelect(RemoteRobot robot, int row, String selection) {
        getOpenshiftConnectorTree().clickRow(row);
        getOpenshiftConnectorTree().rightClickRow(row);
        waitForComponentByXpath(robot, 5, 1, byXpath(getTextXPath(selection)));
        find(ComponentFixture.class, byXpath(getTextXPath(selection))).click();
    }

    public void refreshTree(RemoteRobot robot){
        menuRightClickAndSelect(robot, 0, REFRESH);
    }

    private boolean isViewOpened() {
        try {
            final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
            toolWindowPane.find(ComponentFixture.class, byXpath(OPENSHIFT_BASELABEL));
            LOGGER.info("Openshift view: View is already opened");
            return true;
        } catch (Exception ignored) {
            LOGGER.info("Openshift view: View is not opened");
            return false;
        }
    }
}
