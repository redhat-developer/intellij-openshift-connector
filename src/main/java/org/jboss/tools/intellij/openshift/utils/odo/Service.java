/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

public interface Service {
    String getName();
    String getComponent();
    String getPath();
    void setPath(String path);

    static Service of(String name, String component) {
        return new Service() {
            private String path;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getComponent() {
                return component;
            }

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public void setPath(String path) {
                this.path = path;
            }
        };
    }
}
