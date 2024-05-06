/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Martin Szuc
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.utils.constants;

public class XPathConstants {
    public static final String OPENSHIFT_BASELABEL = "//div[@accessiblename='OpenShift' and @class='BaseLabel' and @text='OpenShift']";
    public static final String GETTING_STARTED_BASELABEL = "//div[@accessiblename='Getting Started' and @class='BaseLabel' and @text='Getting Started']";
    public static final String JTEXT_FIELD = "//div[@class='JTextField']";
    public static final String JPASSWORD_FIELD = "//div[@class='JPasswordField']";
    public static final String CLUSTER_LOGIN_DIALOG = "//div[@accessiblename='Cluster Login' and @class='MyDialog']";
    public static final String TREE_CLASS = "//div[@class='Tree']";
    public static final String MYDIALOG_CLASS = "//div[@class='MyDialog']";
    public static final String NEXT_BROWSE_ALL_THE_DEVFILE_STACKS = "//div[@accessiblename='Next: Browse all the Devfile stacks from Devfile registries >' and @class='JLabel' and @text='Next: Browse all the Devfile stacks from Devfile registries >']";
    public static final String BACK_START_A_COMPONENT_IN_DEVELOPMENT_MODE = "//div[@accessiblename='< Back: Start a component in development mode' and @class='JLabel' and @text='< Back: Start a component in development mode']";
    public static final String LEAVE_FEEDBACK = "//div[@text='Leave feedback']";
    public static final String SELECT_ALL = "//div[contains(@text.key, 'action.$SelectAll.text')]";
    public static final String COPY = "//div[contains(@text.key, 'action.$Copy.text')]";
    public static final String JB_TERMINAL_PANEL = "//div[@class='JBTerminalPanel']";
    public static final String HIDE_BUTTON = "//div[@class='ToolWindowHeader'][.//div[@class='ContentTabLabel']]//div[@myaction.key='tool.window.hide.action.name']";
    public static final String IDE_FRAME_IMPL = "//div[@class='IdeFrameImpl']";
    public static final String JEDITOR_PANE = "//div[@class='JEditorPane']";
    public static final String BACK_BUTTON_GETTING_STARTED = "//div[@accessiblename='<< Getting Started with OpenShift Toolkit' and @class='JLabel' and @text='<< Getting Started with OpenShift Toolkit']";
    public static final String BUTTON_CLASS = "//div[@class='JButton']";
    public static final String OPEN_CONSOLE_DASHBOARD = "//div[@text='Open Console Dashboard']";


    public static String getToolWindowButton(String label) {
        return "//div[@tooltiptext='" + label + "']";
    }
    public static String getTextXPath(String selection) {
        return "//div[@text='" + selection + "']";
    }
}