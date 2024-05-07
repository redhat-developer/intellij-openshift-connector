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


import com.intellij.openapi.util.io.FileUtil;
import java.io.File;
import java.util.Random;
import org.assertj.core.util.Files;

import static org.fest.assertions.Assertions.assertThat;

public class HelmCliInstallTest extends HelmCliTest {

  public void testInstall_should_install_kuberos() throws Exception {
    String releaseName = Charts.CHART_KUBEROS + System.currentTimeMillis();
    try {
      Chart chart = Charts.get(Charts.CHART_KUBEROS, helm); // fail if not found
      // when
      String result = helm.install(releaseName, chart.getName(), chart.getVersion(), null);
      // then
      assertThat(result).containsIgnoringCase("deployed");
    } finally {
      safeUninstall(releaseName);
    }
  }

  public void testInstall_should_install_sysdig_with_values() throws Exception {
    String release = Charts.CHART_SYSDIG + System.currentTimeMillis();
    try {
      Chart chart = Charts.get(Charts.CHART_SYSDIG, helm); // fail if not found
      File file = Files.newTemporaryFile();
      FileUtil.writeToFile(file, "serviceAccount:\n" +
        "  # Create and use serviceAccount resources\n" +
        "  create: true\n" +
        "  # Use this value as serviceAccountName\n" +
        "  name: sysdig");
      // when
      String result = helm.install(release, chart.getName(), chart.getVersion(), file);
      // then
      assertThat(result).containsIgnoringCase("deployed");
    } finally {
      safeUninstall(release);
    }
  }

  public void testUninstall_should_uninstall_kuberos() throws Exception {
    // given
    String releaseName = Charts.CHART_KUBEROS + new Random().nextInt();
    Chart jenkinsChart = Charts.get(Charts.CHART_KUBEROS, helm);
    helm.install(releaseName, jenkinsChart.getName(), jenkinsChart.getVersion(), null);
    // when
    helm.uninstall(releaseName);
    // then
  }
}
