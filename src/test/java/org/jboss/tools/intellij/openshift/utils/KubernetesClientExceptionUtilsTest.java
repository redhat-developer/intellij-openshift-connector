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

import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.fest.assertions.Assertions.assertThat;

public class KubernetesClientExceptionUtilsTest {

  private final KubernetesClientException notFoundClientException = new KubernetesClientException("Dagobah was not found", HttpURLConnection.HTTP_NOT_FOUND, null);

  @Test
  public void should_be_forbidden() {
    KubernetesClientException e = new KubernetesClientException("it's forbidden to contradict the emperor", HttpURLConnection.HTTP_FORBIDDEN, null);
    assertThat(KubernetesClientExceptionUtils.isForbidden(e)).isTrue();
  }

  @Test
  public void should_not_be_forbidden() {
    assertThat(KubernetesClientExceptionUtils.isForbidden(notFoundClientException)).isFalse();
  }

  @Test
  public void should_be_unauthorized() {
    KubernetesClientException e = new KubernetesClientException("yoda is unauthorized to enter the jedi temple", HttpURLConnection.HTTP_UNAUTHORIZED, null);
    assertThat(KubernetesClientExceptionUtils.isUnauthorized(e)).isTrue();
  }

  @Test
  public void should_not_be_unauthorized() {
    assertThat(KubernetesClientExceptionUtils.isForbidden(notFoundClientException)).isFalse();
  }

  @Test
  public void should_be_host_down() {
    IOException hostDownException = new IOException("Host is down");
    KubernetesClientException e = new KubernetesClientException("the rebel base is down", hostDownException);
    assertThat(KubernetesClientExceptionUtils.isHostDown(e)).isTrue();
  }

  @Test
  public void should_not_be_host_down() {
    IOException hostRunningException = new IOException("Host is up and running");
    KubernetesClientException e = new KubernetesClientException("the rebel base is up and running", hostRunningException);
    assertThat(KubernetesClientExceptionUtils.isHostDown(e)).isFalse();
  }

  @Test
  public void should_not_be_host_down_null_cause() {
    KubernetesClientException e = new KubernetesClientException("the rebel base is up and running", null);
    assertThat(KubernetesClientExceptionUtils.isHostDown(e)).isFalse();
  }

  @Test
  public void should_not_be_host_down_different_cause() {
    KubernetesClientException e = new KubernetesClientException("the rebel base is up and running", notFoundClientException);
    assertThat(KubernetesClientExceptionUtils.isHostDown(e)).isFalse();
  }

  @Test
  public void should_be_connection_reset() {
    IOException connectionResetException = new IOException("Is always fear that the connection reset.");
    KubernetesClientException e = new KubernetesClientException("the connection to the rebel base was cut down", connectionResetException);
    assertThat(KubernetesClientExceptionUtils.isConnectionReset(e)).isTrue();
  }

  @Test
  public void should_not_be_connection_reset() {
    IOException connectionEstablishedException = new IOException("Connection is established");
    KubernetesClientException e = new KubernetesClientException("the connection to the rebel base is stable", connectionEstablishedException);
    assertThat(KubernetesClientExceptionUtils.isConnectionReset(e)).isFalse();
  }

  @Test
  public void should_not_be_connection_reset_null_cause() {
    KubernetesClientException e = new KubernetesClientException("the connection to the rebel base is stable", null);
    assertThat(KubernetesClientExceptionUtils.isConnectionReset(e)).isFalse();
  }

  @Test
  public void should_not_be_connection_reset_different_cause() {
    KubernetesClientException e = new KubernetesClientException("the connection to the rebel base is stable", notFoundClientException);
    assertThat(KubernetesClientExceptionUtils.isConnectionReset(e)).isFalse();
  }

  @Test
  public void should_be_could_not_connect() {
    IOException couldNotConnectException = new IOException("Alarming: failed to connect to Dagobah");
    KubernetesClientException e = new KubernetesClientException("Operation: connection to the rebel base failed", couldNotConnectException);
    assertThat(KubernetesClientExceptionUtils.isCouldNotConnect(e)).isTrue();
  }

  @Test
  public void should_not_be_could_not_connect_null_cause() {
    KubernetesClientException e = new KubernetesClientException("Operation: connection to the rebel base failed", null);
    assertThat(KubernetesClientExceptionUtils.isCouldNotConnect(e)).isFalse();
  }

  @Test
  public void should_not_be_could_not_connect_different_cause() {
    KubernetesClientException e = new KubernetesClientException("Operation: connection to the rebel base failed", notFoundClientException);
    assertThat(KubernetesClientExceptionUtils.isCouldNotConnect(e)).isFalse();
  }

}
