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

public class ChartRelease {
  private String name;
  private String namespace;
  private String revision;
  private String updated;
  private String status;
  private String chart;
  private String app_version;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  public String getRevision() {
    return revision;
  }

  public void setUpdated(String updated) {
    this.updated = updated;
  }

  public String getUpdated() {
    return updated;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public void setChart(String chart) {
    this.chart = chart;
  }

  public String getChart() {
    return chart;
  }

  public void setApp_version(String app_version) {
    this.app_version = app_version;
  }

  public String getApp_version() {
    return app_version;
  }
}
