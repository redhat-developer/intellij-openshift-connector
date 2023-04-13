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
package org.jboss.tools.intellij.openshift;

public final class KubernetesLabels {

  private KubernetesLabels() {
    //prevent instantiation
  }

  public static final String COMPONENT_NAME_LABEL = "app.kubernetes.io/instance";

  public static final String COMPONENT_LABEL = "app.kubernetes.io/component";

}
