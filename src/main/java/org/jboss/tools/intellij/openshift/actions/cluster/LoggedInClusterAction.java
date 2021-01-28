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
package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;

public abstract class LoggedInClusterAction extends OdoAction {

    public LoggedInClusterAction() {
        super(ApplicationsRootNode.class);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Object selected = getSelected(getTree(e));
        if (selected instanceof ApplicationsRootNode) {
            e.getPresentation().setVisible(((ApplicationsRootNode) selected).isLogged());
        }
    }
}
