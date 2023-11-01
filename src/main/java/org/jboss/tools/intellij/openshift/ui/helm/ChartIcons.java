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

import java.util.Optional;
import java.util.stream.Stream;

public class ChartIcons {

  private static final String BASE_PATH = "/images/helm/";
  private static final String HELM_ICON = "helm.png";

  public static javax.swing.Icon getIcon(String name) {
    Optional<IconExpression> found = Stream.of(IconExpression.values())
      .filter((IconExpression available) -> available.isMatching(name))
      .findFirst();
    return found
      .map(iconExpression -> IconManager.getInstance().getIcon(iconExpression.filename, ChartIcons.class))
      .orElseGet(() -> IconManager.getInstance().getIcon(BASE_PATH + HELM_ICON, ChartIcons.class));
  }

  private enum IconExpression {
    A10_THUNDER("a10-thunder.png", "A10 Thunder"),
    BACKSTAGE("backstage.png", "Backstage"),
    BROADPEAK("broadpeak.png", "Broadpeak"),
    DATA_GRID("data-grid.png", "Red Hat Data Grid"),
    FLOMESH("flomesh.png", "Flomesh"),
    JENKINS("jenkins.png", "Jenkins"),
    HASHICORP_VAULT("vault.png", "HashiCorp Vault"),
    IBM_OPERATOR_CATALOG("ibm-operator-catalog.png", "IBM Operator Catalog"),
    IBM_ORDER_MANAGEMENT("ibm-order-management.png", "IBM Order Management"),
    IBM_SPECTRUM_PROTECT("ibm-spectrum-protect.png", "IBM Spectrum Protect"),
    IBM_CLOUD_OBJECT_STORAGE("ibm-cloud-object-storage.png", "IBM Cloud Object Storage"),
    INFINISPAN("infinispan.png", "Infinispan"),
    EAP("eap.png", "JBoss EAP"),
    KYVERNO("kyverno.png", "Kubernetes Native Policy Management"),
    NEARBY_ONE("nearby-one.png", "NearbyOne Controller"),
    NODE_RED("node-red.png", "NodeRed"),
    FIWARE("fiware.png", "FIWARE FOUNDATION"),
    SOLACE_PUBSUB("solace-pubsub.png", "Solace PubSub+"),
    RAFAY("rafay.png", "Rafay"),
    CRYOSTAT("cryostat.png", "Cryostat"),
    REDHAT("redhat.png", "Red Hat Developer Hub"),
    WILDFLY("wildfly.png", "WildFly"),
    YUGAWARE("yugaware.png", "YugabyteDB");

    private final String filename;
    private final String substring;

    IconExpression(String filename, String substring) {
      this.filename = BASE_PATH + filename;
      this.substring = substring;
    }

    public boolean isMatching(String chartText) {
      /* using regex was too slow, using simple substring matching */
      return chartText.contains(substring);
    }
  }

}
