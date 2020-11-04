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

public enum ComponentSourceType {
    LOCAL("Local"),
    GIT("Git"),
    BINARY("Binary");

    private final String label;

    ComponentSourceType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static ComponentSourceType fromAnnotation(String annotation) {
        switch (annotation) {
            case "git":
                return GIT;
            case "binary":
                return BINARY;
            case "local":
            default:
                return LOCAL;
        }
    }
}
