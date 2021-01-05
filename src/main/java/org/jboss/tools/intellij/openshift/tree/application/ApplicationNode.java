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
package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.tree.IconTreeNode;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Application;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;


public class ApplicationNode extends LazyMutableTreeNode implements IconTreeNode {
    public ApplicationNode(Application application) {
        super(application);
    }

    @Override
    public void load() {
        super.load();
        try {
            ApplicationsRootNode rootNode = (ApplicationsRootNode) getRoot();
            Odo odo = rootNode.getOdo();
            try {
                odo.getComponents(getParent().toString(), toString()).forEach(dc -> add(new ComponentNode(dc)));
            } catch (KubernetesClientException e) {
                add(new DefaultMutableTreeNode("Failed to load application deployment configs"));
            }
            odo.getServices(getParent().toString(), toString()).forEach(si -> add(new ServiceNode(si)));
        } catch (IOException e) {
            add(new DefaultMutableTreeNode("Failed to load application"));
        }
    }

    @Override
    public String toString() {
        return ((Application) userObject).getName();
    }

    @Override
    public String getIconName() {
        return "/images/application.png";
    }
}
