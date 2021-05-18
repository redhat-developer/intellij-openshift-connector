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
package org.jboss.tools.intellij.openshift.actions.component;

import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;

public abstract class PushedComponentAction extends ContextAwareComponentAction {
    @Override
    public boolean isVisible(Object selected) {
        boolean isVisible = super.isVisible(selected);
        if (isVisible) {
            isVisible = ((ComponentNode)selected).getComponent().getState() == ComponentState.PUSHED;
        }
        return isVisible;
    }
}
