/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.url;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.URLNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.URL;

import javax.swing.tree.TreePath;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class OpenInBrowserAction extends OdoAction {
    public OpenInBrowserAction() {
        super(URLNode.class);
    }

    @Override
    protected String getTelemetryActionName() {
        return "open URL";
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            URL url = ((URLNode) selected).getUrl();
            visible = url.getState() != URL.State.NOT_PUSHED;
        }
        return visible;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
        BrowserUtil.open(getURL(((URLNode) selected).getUrl()));
        sendTelemetryResults(TelemetryResult.SUCCESS);
    }

    protected String getURL(URL url) {
        return url.getProtocol() + "://" + url.getHost();
    }
}
