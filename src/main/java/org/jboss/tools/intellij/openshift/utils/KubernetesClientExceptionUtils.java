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

import io.fabric8.kubernetes.client.KubernetesClientException;

import java.net.HttpURLConnection;

public class KubernetesClientExceptionUtils {

  private KubernetesClientExceptionUtils() {}

  public static boolean isForbidden(KubernetesClientException e) {
    return HttpURLConnection.HTTP_FORBIDDEN == e.getCode();
  }

  public static boolean isUnauthorized(KubernetesClientException e) {
    return HttpURLConnection.HTTP_UNAUTHORIZED == e.getCode();
  }

  public static boolean isHostDown(KubernetesClientException e) {
    if (e.getCause() == null) {
      return false;
    }
    return messageContains("host is down", e.getCause());
  }

  public static boolean isConnectionReset(KubernetesClientException e) {
    if (e.getCause() == null) {
      return false;
    }
    return messageContains("connection reset", e.getCause());
  }

  public static boolean isCouldNotConnect(KubernetesClientException e) {
    if (e.getCause() == null) {
      return false;
    }
    return messageContains("failed to connect", e.getCause());
  }

  public static boolean messageContains(String message, Throwable e) {
    return e != null
      && e.getMessage() != null
      && e.getMessage().toLowerCase().contains(message);
  }

}

