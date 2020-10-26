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

public interface Component {
    String getName();

    ComponentState getState();

    void setState(ComponentState state);

    String getPath();

    void setPath(String path);

    default boolean hasContext() {
        return getPath() != null;
    }

    ComponentInfo getInfo();

    class ComponentImpl implements Component {
        private final String name;
        private ComponentState state;
        private String path;
        private final ComponentInfo info;

        private ComponentImpl(String name, ComponentState state, String path, ComponentInfo info) {
            this.name = name;
            this.state = state;
            this.path = path;
            this.info = info;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ComponentState getState() {
            return state;
        }

        @Override
        public void setState(ComponentState state) {
            this.state = state;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public ComponentInfo getInfo() {
            return info;
        }
    }

    static Component of(String name) {
        return of(name, ComponentState.NO_CONTEXT, null);
    }

    static Component of(String name, ComponentState state, ComponentInfo info) {
        return of(name, state, null, info);
    }

    static Component of(String name, ComponentState state, String path, ComponentInfo info) {
        return new ComponentImpl(name, state, path, info);
    }
}
