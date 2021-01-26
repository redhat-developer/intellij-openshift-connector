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
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import io.fabric8.kubernetes.api.model.NamedContext;
import org.jboss.tools.intellij.openshift.actions.TreeAction;
import org.jboss.tools.intellij.openshift.tree.ClustersTreeModel;

import javax.swing.tree.TreePath;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetCurrentClusterAction extends TreeAction {

    private static final Logger LOG = LoggerFactory.getLogger(SetCurrentClusterAction.class);

    public SetCurrentClusterAction() {
        super(NamedContext.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        NamedContext context = (NamedContext) selected;
        ClustersTreeModel model = (ClustersTreeModel) getTree(anActionEvent).getModel();
        model.getConfig().setCurrentContext(context.getName());
        try {
            ConfigHelper.saveKubeConfig(model.getConfig());
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
