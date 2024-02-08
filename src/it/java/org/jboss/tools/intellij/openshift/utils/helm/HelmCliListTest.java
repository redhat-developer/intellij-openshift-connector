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


import java.util.List;
import java.util.Random;

public class HelmCliListTest extends HelmCliTest {

  public void testList_should_list_kuberos() throws Exception {
    // given openshift repo was added to helm
    String releaseName = Charts.CHART_KUBEROS + new Random().nextInt();
    try {
      Chart jenkinsChart = Charts.get(Charts.CHART_KUBEROS, helm);
      helm.install(releaseName, jenkinsChart.getName(), jenkinsChart.getVersion(), null);
      // when
      List<ChartRelease> releases = helm.list();
      // then
      assertTrue(releases.stream().anyMatch(chart -> releaseName.equals(chart.getName())));
    } finally {
      safeUninstall(releaseName);
    }
  }

  public void testUninstall_should_uninstall_vertx() throws Exception {
    // given
    String releaseName = Charts.CHART_KUBEROS + new Random().nextInt();
    Chart jenkinsChart = Charts.get(Charts.CHART_KUBEROS, helm);
    helm.install(releaseName, jenkinsChart.getName(), jenkinsChart.getVersion(), null);
    // when
    helm.uninstall(releaseName);
    // then
  }
}
