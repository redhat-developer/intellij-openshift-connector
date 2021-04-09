/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryHandler;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;

import javax.swing.tree.TreePath;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class TreeAction extends AnAction implements TelemetryHandler {

    private Class[] filters;

    private TelemetrySender telemetrySender;

    protected TreeAction(Class... filters) {
        this.filters = filters;
    }

    protected Tree getTree(AnActionEvent e) {
        return (Tree) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
    }

    @Override
    public void update(AnActionEvent e) {
        boolean visible = false;
        Optional<TreePath> selectedPath = getSelectedPath(getTree(e));
        if (selectedPath.isPresent()) {
            visible = isVisible(selectedPath.get().getLastPathComponent());
        }
        e.getPresentation().setVisible(visible);
    }

    public boolean isVisible(Object selected) {
        return Stream.of(filters).anyMatch(cl -> cl.isAssignableFrom(selected.getClass()));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Optional<TreePath> selectedPath = getSelectedPath(getTree(e));
        if (selectedPath.isPresent()) {
            Object selected = selectedPath.get().getLastPathComponent();
            telemetrySender = new TelemetrySender(getTelemetryActionName());
            actionPerformed(e, selectedPath.get(), selected);
        }
    }

    public Optional<TreePath> getSelectedPath(Tree tree) {
        return Optional.ofNullable(tree.getSelectionModel().getSelectionPath());
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected);

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
