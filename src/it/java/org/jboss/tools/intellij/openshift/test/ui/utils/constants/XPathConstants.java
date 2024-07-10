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
    public static final String BACK_BUTTON_GETTING_STARTED = "//div[@accessiblename='<< Getting Started with OpenShift Toolkit' and @class='JLabel' and @text='<< Getting Started with OpenShift Toolkit']";
    public static final String BACK_START_A_COMPONENT_IN_DEVELOPMENT_MODE = "//div[@accessiblename='< Back: Start a component in development mode' and @class='JLabel' and @text='< Back: Start a component in development mode']";
    public static final String BUTTON_CANCEL = "//div[@text='Cancel']";
    public static final String BUTTON_CHANGE = "//div[@text='Change']";
    public static final String BUTTON_CLASS = "//div[@class='JButton']";
    public static final String BUTTON_CREATE = "//div[@text='Create']";
    public static final String BUTTON_NEXT = "//div[@text='Next']";
    public static final String BUTTON_NO = "//div[@text='No']";
    public static final String BUTTON_OK = "//div[@text='OK']";
    public static final String BUTTON_PREVIOUS = "//div[@text='Previous']";
    public static final String BUTTON_YES = "//div[@text='Yes']";
    public static final String COPY = "//div[contains(@text.key, 'action.$Copy.text')]";
    public static final String GETTING_STARTED_BASELABEL = "//div[@accessiblename='Getting Started' and @class='BaseLabel' and @text='Getting Started']";
    public static final String HIDE_BUTTON = "//div[@class='ToolWindowHeader'][.//div[@class='ContentTabLabel']]//div[@myaction.key='tool.window.hide.action.name']";
    public static final String IDE_FRAME_IMPL = "//div[@class='IdeFrameImpl']";
    public static final String JBTEXTFIELD = "//div[@class='JBTextField']";
    public static final String JB_TERMINAL_PANEL = "//div[@class='JBTerminalPanel']";
    public static final String JCHECKBOX = "//div[@class='JCheckBox']";
    public static final String JEDITOR_PANE = "//div[@class='JEditorPane']";
    public static final String JPASSWORD_FIELD = "//div[@class='JPasswordField']";
    public static final String JTEXT_FIELD = "//div[@class='JTextField']";
    public static final String LEAVE_FEEDBACK = "//div[@text='Leave feedback']";
    public static final String MYDIALOG_CLASS = "//div[@class='MyDialog']";
    public static final String NEXT_BROWSE_ALL_THE_DEVFILE_STACKS = "//div[@accessiblename='Next: Browse all the Devfile stacks from Devfile registries >' and @class='JLabel' and @text='Next: Browse all the Devfile stacks from Devfile registries >']";
    public static final String OPEN_CONSOLE_DASHBOARD = "//div[@text='Open Console Dashboard']";
    public static final String OPENSHIFT_BASELABEL = "//div[@accessiblename='OpenShift' and @class='BaseLabel' and @text='OpenShift']";
    public static final String SELECT_ALL = "//div[contains(@text.key, 'action.$SelectAll.text')]";
    public static final String TEXT_FIELD_W_AUTO_COMPLETION = "//div[@class='TextFieldWithAutoCompletion']";
    public static final String TREE_CLASS = "//div[@class='Tree']";
    public static final String BUTTON_HELP = "//div[@accessiblename='Help' and @class='JButton' and @text='Help']";
    public static final String JLIST = "//div[@class='JList']";
    public static final String BUTTON_PASTE_LOGIN_COMMAND = "//div[@text='Paste Login Command']";
    public static final String GETTING_STARTED_ACTION_MENU_ITEM = "//div[@accessiblename='Getting Started' and @class='ActionMenuItem' and @text='Getting Started']";


    public static String getToolWindowButton(String label) {
        return "//div[@tooltiptext='" + label + "']";
    }

    public static String getTextXPath(String selection) {
        return "//div[@text='" + selection + "']";
    }
}