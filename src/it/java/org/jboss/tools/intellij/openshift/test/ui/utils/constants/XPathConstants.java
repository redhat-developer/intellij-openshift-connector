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
package org.jboss.tools.intellij.openshift.test.ui.utils.constants;

public class XPathConstants {
    public static final String OPENSHIFT_BASELABEL = "//div[@accessiblename='OpenShift' and @class='BaseLabel' and @text='OpenShift']";
    public static final String GETTING_STARTED_BASELABEL = "//div[@accessiblename='Getting Started' and @class='BaseLabel' and @text='Getting Started']";
    public static final String JTEXT_FIELD = "//div[@class='JTextField']";
    public static final String JPASSWORD_FIELD = "//div[@class='JPasswordField']";
    public static final String CLUSTER_LOGIN_DIALOG = "//div[@accessiblename='Cluster login' and @class='MyDialog']";
    public static final String TREE_CLASS = "//div[@class='Tree']";
    public static final String IDE_FRAME_IMPL = "//div[@class='IdeFrameImpl']";
    public static final String JEDITOR_PANE = "//div[@class='JEditorPane']";
    public static final String BACK_BUTTON_GETTING_STARTED = "//div[@accessiblename='<< Getting Started with OpenShift Toolkit' and @class='JLabel' and @text='<< Getting Started with OpenShift Toolkit']";


    public static String getToolWindowButton(String label) {
        return "//div[@tooltiptext='" + label + "']";
    }
    public static String getTextXPath(String selection) {
        return "//div[@text='" + selection + "']";
    }
}