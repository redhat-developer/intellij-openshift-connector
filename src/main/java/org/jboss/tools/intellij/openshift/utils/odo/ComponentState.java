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

public enum ComponentState {
    PUSHED("\u25C9 pushed"),
    NOT_PUSHED("\u25CE not pushed"),
    NO_CONTEXT("\u2718 no context");

    private final String label;

    ComponentState(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static ComponentState fromState(String state) {
        switch (state) {
            case "Pushed":
                return PUSHED;
            case "Not Pushed":
                return NOT_PUSHED;
            default:
                return NO_CONTEXT;
        }
    }
}
