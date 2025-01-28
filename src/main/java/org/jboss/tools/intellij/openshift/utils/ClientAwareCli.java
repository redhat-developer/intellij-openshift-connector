/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import io.fabric8.kubernetes.client.KubernetesClient;

public class ClientAwareCli extends Cli{

  protected final KubernetesClient client;

  protected ClientAwareCli(String command, KubernetesClient client) {
    super(command);
    this.client = client;
  }
}
