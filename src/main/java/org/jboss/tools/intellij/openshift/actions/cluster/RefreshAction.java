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
package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;

import javax.swing.tree.TreePath;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;

public class RefreshAction extends StructureTreeAction {
    public RefreshAction() {
        super(ApplicationsRootNode.class);
    }

    public static void execute(ParentableNode<?> node) {
        RefreshAction action = (RefreshAction) ActionManager.getInstance().getAction(RefreshAction.class.getName());
        action.doActionPerformed(node.getRoot());
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        doActionPerformed(NodeUtils.getRoot(selected));
    }

    public void doActionPerformed(ApplicationsRootNode root) {
        if (root == null) {
          return;
        }
        runWithProgress((ProgressIndicator progress) -> {
              root.refresh();
              TelemetryService.instance().getBuilder()
                .action(PREFIX_ACTION + "refresh cluster")
                .success()
                .send();
          },
          "Refreshing...",
          root.getProject());
    }
}
