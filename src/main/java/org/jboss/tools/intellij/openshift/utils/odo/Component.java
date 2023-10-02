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

import java.util.Objects;

public interface Component {
    String getName();

    ComponentFeatures getLiveFeatures();

    String getPath();

    void setPath(String path);

    default boolean hasContext() {
        return getPath() != null;
    }

    ComponentInfo getInfo();

    void setInfo(ComponentInfo componentInfo);

    class ComponentImpl implements Component {
        private final String name;
        private ComponentFeatures state;
        private String path;
        private ComponentInfo info;

        private ComponentImpl(String name, ComponentFeatures state, String path, ComponentInfo info) {
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
        public ComponentFeatures getLiveFeatures() {
            return state;
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

        @Override
        public void setInfo(ComponentInfo componentInfo) {
            this.info = componentInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ComponentImpl component = (ComponentImpl) o;
            return Objects.equals(name, component.name) && Objects.equals(state, component.state) && Objects.equals(path, component.path) && Objects.equals(info, component.info);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, state, path, info);
        }
    }

    static Component of(String name, ComponentFeatures state, ComponentInfo info) {
        return of(name, state, null, info);
    }

    static Component of(String name, ComponentFeatures state, String path, ComponentInfo info) {
        return new ComponentImpl(name, state, path, info);
    }
}
