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
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class OdoAction extends StructureTreeAction {
  public OdoAction(Class... filters) {
    super(filters);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
    try {
      this.actionPerformed(anActionEvent, path, selected, getOdo(anActionEvent));
    } catch (IOException e) {
      Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
    }
  }

    private Odo getOdo(AnActionEvent anActionEvent) throws IOException {
        Tree tree = getTree(anActionEvent);
        return ((ApplicationsRootNode)((ApplicationsTreeStructure)tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).getRootElement()).getOdo();
    }

    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
  }
}
