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

import com.intellij.openapi.util.Pair;

import java.io.IOException;
import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;

public class Charts {

  public static final String CHART_KUBEROS = "kuberos";
  public static final String CHART_SYSDIG = "sysdig";

  public static final Pair<String, String> REPOSITORY_STABLE = new Pair("stable", "https://charts.helm.sh/stable");
  public static final Pair<String, String> REPOSITORY_OPENSHIFT = new Pair("openshift", "https://charts.openshift.io/");

  public static Chart get(String name, Helm helm) throws Exception {
    Optional<Chart> found = helm.search(name).stream().findFirst();
    assertThat(found.isPresent()).isTrue();
    return found.get();
  }

  public static void addRepository(Pair<String, String> pair, Helm helm) throws IOException {
    helm.addRepo(pair.first, pair.second, null);
  }

}
