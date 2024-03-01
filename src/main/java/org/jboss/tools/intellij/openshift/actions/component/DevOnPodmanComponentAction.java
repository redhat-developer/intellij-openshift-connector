/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeature;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DevOnPodmanComponentAction extends FeatureComponentAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevOnPodmanComponentAction.class);

    public DevOnPodmanComponentAction() {
        super(ComponentFeature.DEV_ON_PODMAN);
    }

    @Override
    protected boolean needCustomizedPresentation() {
        return true;
    }

    @Override
    protected String getCustomizedPresentation(Component component) {
        if (component.getLiveFeatures().is(ComponentFeature.DEV_ON_PODMAN)) {
            return "Stop " + getActionName();
        } else {
            return "Start " + getActionName();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if (e.getPresentation().isVisible()) {
            Object node = adjust(getSelected(getTree(e)));
            if (!(node instanceof ComponentNode)) {
                return;
            }
            ComponentNode componentNode = (ComponentNode) node;
            Odo odo = componentNode.getRoot().getOdo().getNow(null);
            if (odo == null) {
                return;
            }
            try {
                e.getPresentation().setEnabled(odo.checkPodman());
            } catch (IOException ex) {
                LOGGER.warn("Could not update {}", componentNode.getName(), ex);
            }
        }

    }
}
