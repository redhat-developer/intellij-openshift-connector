/*************************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. and others.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.intellij.openshift.oauth;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.jboss.tools.intellij.openshift.oauth.model.IAuthorizationServer;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthUtilsTest {
  @Test
  public void testDecodeEmailFromToken() {
    IAuthorizationServer server = mock(IAuthorizationServer.class);
    assertThrows(IllegalArgumentException.class, () ->
      OAuthUtils.decodeEmailFromToken(server, ""));
    assertThrows(MalformedJwtException.class, () ->
      OAuthUtils.decodeEmailFromToken(server, "1234"));
    String noneAlgHeader = Base64.getEncoder().encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
    String payload = Base64.getEncoder().encodeToString("payload".getBytes(StandardCharsets.UTF_8));
    assertThrows(UnsupportedJwtException.class, () ->
      OAuthUtils.decodeEmailFromToken(server, noneAlgHeader + '.' + payload + '.'));
    String wrongAlgHeader = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));
    assertThrows(MalformedJwtException.class, () ->
      OAuthUtils.decodeEmailFromToken(server, wrongAlgHeader + '.' + payload + '.'));
  }

  @Test
  public void testGetDeployment() {
    IAuthorizationServer server = mock(IAuthorizationServer.class);
    when(server.getRealm()).thenReturn("");
    when(server.getClientId()).thenReturn("");
    when(server.getURL()).thenReturn("");
    assertNotNull(OAuthUtils.getDeployment(server));
  }

}