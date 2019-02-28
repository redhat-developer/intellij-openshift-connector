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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdoConfig {
  public static class Application {
    private boolean active;
    private String activeComponent;
    private String name;
    private String project;

    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
    }

    public String getActiveComponent() {
      return activeComponent;
    }

    public void setActiveComponent(String activeComponent) {
      this.activeComponent = activeComponent;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getProject() {
      return project;
    }

    public void setProject(String project) {
      this.project = project;
    }
  }

  private List<Application> activeApplications = new ArrayList<>();

  public List<Application> getActiveApplications() {
    return activeApplications;
  }

  public void setActiveApplications(List<Application> activeApplications) {
    this.activeApplications = activeApplications;
  }
}
