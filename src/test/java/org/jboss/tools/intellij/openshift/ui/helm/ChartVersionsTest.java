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

import org.jboss.tools.intellij.openshift.utils.helm.Chart;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class ChartVersionsTest {

  @Test
  public void versions_should_be_sorted_numerically() {
    // given
    List<String> versions = Arrays.asList(
      "1.11.1",
      "1.32.5",
      "1.18.0"
    );
    ChartVersions chart = new ChartVersions(
      "yoda",
      "a jedi",
      versions);
    // when
    List<String> sorted = chart.getVersions();
    // then
    assertThat(sorted).isEqualTo(Arrays.asList(
      "1.32.5",
      "1.18.0",
      "1.11.1"
    ));
  }

  @Test
  public void versions_should_sort_alphanumeric_version_behind_numeric_version() {
    // given
    List<String> versions = Arrays.asList(
      "4.9.7",
      "4.9.6",
      "4.9.7-debian-9",
      "5.0.1"
    );
    ChartVersions chart = new ChartVersions(
      "yoda",
      "a jedi",
      versions);
    // when
    List<String> sorted = chart.getVersions();
    // then
    assertThat(sorted).isEqualTo(Arrays.asList(
      "5.0.1",
      "4.9.7",
      "4.9.7-debian-9",
      "4.9.6"
    ));
  }

  @Test
  public void toChartVersions_should_group_by_chart_name() {
    // given
    List<Chart> charts = Arrays.asList(
      new Chart("yoda", "1.0", "jedi"),
      new Chart("yoda", "1.1", "jedi"),
      new Chart("yoda", "1.2", "jedi"),
      new Chart("lord/sidius", "1.0", "jedi"),
      new Chart("lord/vader", "1.1", "jedi"),
    new Chart("lord/vader", "1.2", "jedi")
    );
    // when
    List<ChartVersions> byName = ChartVersions.toChartVersions(charts);
    // then
    assertThat(byName).hasSize(3);
  }
}
