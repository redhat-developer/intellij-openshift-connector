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
package org.jboss.tools.intellij.openshift.utils.odo;

import java.util.HashSet;
import java.util.Set;

public class ComponentState {
    public static final String DEV_STATE = "Dev";
    public static final String DEPLOY_STATE = "Deploy";
    private static final String DEBUG_STATE = "Debug";

    private Set<String> states = new HashSet<>();

    public void addState(String state) {
        states.add(state);
    }

    public void removeState(String state) {
        states.remove(state);
    }

    public void setStates(Set<String> states) {
        this.states = states;
    }

    private boolean is(String state) {
        return states.contains(state);
    }

    public boolean isDevRunning() {
        return is(DEV_STATE);
    }

    public boolean isDeployRunning() {
        return is(DEPLOY_STATE);
    }

    public boolean isDebugRunning() {
        return is(DEBUG_STATE);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        if (isDevRunning()) {
            first = append(builder, first, DEV_STATE);
        }
        if (isDebugRunning()) {
            first = append(builder, first, DEBUG_STATE);
        }
        if (isDeployRunning()) {
            first = append(builder, first, DEPLOY_STATE);
        }
        return builder.toString();
    }

    private boolean append(StringBuilder builder, boolean first, String label) {
        if (!first) {
            builder.append(',');
        }
        builder.append(label);
        return false;
    }

    public boolean isOnCluster() {
        return isDevRunning() || isDebugRunning() || isDeployRunning();
    }
}
