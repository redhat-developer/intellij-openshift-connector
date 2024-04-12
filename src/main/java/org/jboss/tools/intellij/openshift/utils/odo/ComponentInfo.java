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

import java.util.ArrayList;
import java.util.List;

public interface ComponentInfo {

  String getComponentTypeName();

  String getLanguage();

  ComponentKind getComponentKind();

  List<ComponentFeature.Mode> getSupportedFeatures();

  boolean isLocalPodmanPresent();

  class Builder {
    private String componentTypeName;

    private ComponentKind kind;

    private List<ComponentFeature.Mode> supportedFeatures = new ArrayList<>();

    private String language;

    private boolean isLocalPodmanPresent;

    public Builder withComponentTypeName(String componentTypeName) {
      this.componentTypeName = componentTypeName;
      return this;
    }

    public Builder withComponentKind(ComponentKind kind) {
      this.kind = kind;
      return this;
    }

    public Builder withSupportedFeatures(List<ComponentFeature.Mode> features) {
      this.supportedFeatures = features;
      return this;
    }

    public Builder withLanguage(String language) {
      this.language = language;
      return this;
    }

    public Builder withLocalPodmanPresence(boolean isLocalPodmanPresent) {
      this.isLocalPodmanPresent = isLocalPodmanPresent;
      return this;
    }

    public ComponentInfo build() {
      return new ComponentInfo() {

        @Override
        public String getComponentTypeName() {
          return componentTypeName;
        }

        @Override
        public String getLanguage() {
          return language;
        }

        @Override
        public ComponentKind getComponentKind() {
          return kind;
        }

        @Override
        public List<ComponentFeature.Mode> getSupportedFeatures() {
          return supportedFeatures;
        }

        @Override
        public boolean isLocalPodmanPresent() {
          return isLocalPodmanPresent;
        }
      };
    }
  }
}
