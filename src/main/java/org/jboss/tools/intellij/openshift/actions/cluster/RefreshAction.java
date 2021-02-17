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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;

import javax.swing.tree.TreePath;

public class RefreshAction extends StructureTreeAction {
    public RefreshAction() {
        super(ApplicationsRootNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        selected = getElement(selected);
        ApplicationsTreeStructure structure = (ApplicationsTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY);
        structure.fireModified(selected);
    }
}
