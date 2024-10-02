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

import com.intellij.ui.IconManager;
import org.jboss.tools.intellij.openshift.utils.helm.ChartRelease;

import javax.swing.Icon;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartIcons {

  private static final Path BASE_PATH = Paths.get("images", "helm");
  private static final String HELM_ICON = "helm.svg";

  public static Icon getHelmIcon() {
    // IC-2023.3: IconManager.getInstance().getIcon(BASE_PATH.resolve(HELM_ICON_SVG).toString(), ChartIcons.class.getClassLoader())
    return IconManager.getInstance().getIcon(BASE_PATH.resolve(HELM_ICON).toString(), ChartIcons.class);
  }

  public static Icon getIcon(ChartVersions chart) {
    return getIcon(chart.getName() + chart.getDescription());
  }

  public static Icon getIcon(ChartRelease chart) {
    return getIcon(chart.getChart());
  }

  private static Icon getIcon(String name) {
    Optional<IconExpression> found = Stream.of(IconExpression.values())
      .filter((IconExpression available) -> available.isMatching(name))
      .findFirst();
    return found
      // IC-2023.3: IconManager.getInstance().getIcon(BASE_PATH.resolve(HELM_ICON_SVG).toString(), ChartIcons.class.getClassLoader())
      .map(iconExpression -> IconManager.getInstance().getIcon(iconExpression.filename, ChartIcons.class))
       .orElseGet(ChartIcons::getHelmIcon);
  }

  private enum IconExpression {
    A10_THUNDER("a10-thunder.png", "A10network"),
    BACKSTAGE("backstage.png", "backstage"),
    BROADPEAK("broadpeak.png", "broadpeak"),
    DATA_GRID("data-grid.png", "data-grid-"),
    FLOMESH("flomesh.png", "Flomesh"),
    JENKINS("jenkins.png", "Jenkins"),
    HASHICORP_VAULT("vault.png", "hashiCorp-vault"),
    IBM_OPERATOR_CATALOG("ibm-operator-catalog.png", "ibm-operator-catalog"),
    IBM_ORDER_MANAGEMENT("ibm-order-management.png", "ibm-oms-"),
    IBM_SPECTRUM_PROTECT("ibm-spectrum-protect.png", "ibm-spectrum-protect"),
    IBM_CLOUD_OBJECT_STORAGE("ibm-cloud-object-storage.png", "ibm-object-storage"),
    INFINISPAN("infinispan.png", "Infinispan"),
    EAP("eap.png", "-eap"),
    KYVERNO("kyverno.png", "-kyverno"),
    NEARBY_ONE("nearby-one.png", "Nearby"),
    NODE_RED("node-red.png", "NodeRed"),
    FIWARE("fiware.png", "orion-ld"),
    SOLACE_PUBSUB("solace-pubsub.png", "pubsubplus-"),
    RAFAY("rafay.png", "rafay"),
    CRYOSTAT("cryostat.png", "cryostat"),
    REDHAT("redhat.png", "developer-hub"),
    WILDFLY("wildfly.png", "wildFly"),
    YUGAWARE("yugaware.png", "yugaware");

    private final String filename;
    private final String substring;

    IconExpression(String filename, String substring) {
      this.filename = BASE_PATH.resolve(filename).toString();
      this.substring = substring;
    }

    public boolean isMatching(String chartText) {
      /* using regex was too slow, using simple substring matching */
      return chartText.toLowerCase().contains(substring.toLowerCase());
    }
  }
}
