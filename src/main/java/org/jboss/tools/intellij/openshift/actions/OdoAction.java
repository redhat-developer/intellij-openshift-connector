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
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryHandler;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;

public abstract class OdoAction extends StructureTreeAction implements TelemetryHandler {

    protected TelemetrySender telemetrySender;

    protected OdoAction(Class... filters) {
        super(filters);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        telemetrySender = new TelemetrySender(getTelemetryActionName());
        this.actionPerformed(anActionEvent, path, getElement(selected), getOdo(anActionEvent));
    }

    private Odo getOdo(AnActionEvent anActionEvent) {
        Tree tree = getTree(anActionEvent);
        return ((ApplicationsRootNode) ((ApplicationsTreeStructure) tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).getRootElement()).getOdo();
    }

    protected abstract void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo);

    protected abstract String getTelemetryActionName();

    public void sendTelemetryResults(TelemetryService.TelemetryResult result) {
        telemetrySender.sendTelemetryResults(result);
    }

    @Override
    public void sendTelemetryError(String message) {
        telemetrySender.sendTelemetryError(message);
    }

    @Override
    public void sendTelemetryError(Exception exception) {
        telemetrySender.sendTelemetryError(exception);
    }

}
