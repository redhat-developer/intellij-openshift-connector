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
package org.jboss.tools.intellij.openshift.ui.helm;

import com.intellij.openapi.util.Pair;
import com.intellij.webcore.packaging.PackageVersionComparator;
import org.jboss.tools.intellij.openshift.utils.helm.Chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartVersions {

  public static List<ChartVersions> toChartVersions(final List<Chart> charts) {
    final Map<Pair<String, String>, Map<String, List<Chart>>> chartsByVersion = charts.stream()
      .collect(
        // group by (name, description) & version
        Collectors.groupingBy(chart -> new Pair<>(chart.getName(), chart.getDescription()),
          Collectors.groupingBy(Chart::getVersion)));
    return chartsByVersion.entrySet().stream()
      .map(entry -> {
        String name = entry.getKey().getFirst();
        String description = entry.getKey().getSecond();
        List<String> versions = new ArrayList<>(entry.getValue().keySet());
        return new ChartVersions(name, description, versions);
      })
      .collect(Collectors.toList());
  }

  private final String name;
  private final String description;
  private final List<String> versions;

  public ChartVersions(final String name, final String description, final List<String> versions) {
    this.name = name;
    this.description = description;
    this.versions = sortVersions(versions);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getVersions() {
    return versions;
  }

  private List<String> sortVersions(List<String> versions) {
    return versions.stream()
      .sorted(new PackageVersionComparator().reversed())
      .collect(Collectors.toList());
  }
}
