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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentFeatures {

  private final Set<ComponentFeature> features = new HashSet<>();

  public ComponentFeatures(ComponentFeature feature) {
    addFeature(feature);
  }

  public ComponentFeatures() {}

  public void addFeature(ComponentFeature feature) {
    features.add(feature);
  }

  public void removeFeature(ComponentFeature feature) {
    features.remove(feature);
  }

  public boolean is(ComponentFeature feature) {
    return features.contains(feature);
  }

  public boolean isDev() {
    return is(ComponentFeature.DEV) || is(ComponentFeature.DEV_ON_PODMAN);
  }

  public boolean isDeploy() {
    return is(ComponentFeature.DEPLOY);
  }

  public boolean isDebug() {
    return features.stream().anyMatch(DebugComponentFeature.class::isInstance);
  }

  @Override
  public String toString() {
    return features.stream().map(f -> f.getMode().getLabel()).collect(Collectors.joining(", "));
  }

  public boolean isOnCluster() {
    return isDev() || isDebug() || isDeploy();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ComponentFeatures that = (ComponentFeatures) o;
    return Objects.equals(features, that.features);
  }

  @Override
  public int hashCode() {
    return Objects.hash(features);
  }

  public boolean isEmpty() {
    return features.isEmpty();
  }

  public int size() {
    return features.size();
  }
}
