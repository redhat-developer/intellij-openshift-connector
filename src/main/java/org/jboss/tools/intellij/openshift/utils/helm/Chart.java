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
package org.jboss.tools.intellij.openshift.utils.helm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Chart {
  private String name;
  private String version;
  private String description;

  public Chart() {
  }

  public Chart(String name, String version, String description) {
    this.name = name;
    this.version = version;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Chart)) return false;
    Chart chart = (Chart) o;
    return Objects.equals(name, chart.name)
      && Objects.equals(version, chart.version)
      && Objects.equals(description, chart.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, version, description);
  }

}
