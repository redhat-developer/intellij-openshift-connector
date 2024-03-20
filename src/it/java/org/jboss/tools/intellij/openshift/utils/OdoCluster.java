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
package org.jboss.tools.intellij.openshift.utils;

import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;

public class OdoCluster {

  public static final OdoCluster INSTANCE = new OdoCluster();

  private static final String CLUSTER_URL = System.getenv("CLUSTER_URL");

  private static final String CLUSTER_USER = System.getenv("CLUSTER_USER");

  private static final String CLUSTER_PASSWORD = System.getenv("CLUSTER_PASSWORD");

    public boolean login(Odo odo) throws IOException {
    if (CLUSTER_URL != null && !odo.getMasterUrl().toString().startsWith(CLUSTER_URL)) {
      odo.login(CLUSTER_URL, CLUSTER_USER, CLUSTER_PASSWORD.toCharArray(), null);
      return true;
    } else {
      return false;
    }
  }

}
