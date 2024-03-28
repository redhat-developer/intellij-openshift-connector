/*************************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ************************************************************************************/
package org.jboss.tools.intellij.openshift.oauth;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.jboss.tools.intellij.openshift.oauth.model.IAuthorizationServer;
import org.junit.Test;
import org.keycloak.adapters.KeycloakDeployment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthUtilsTest {

  @Test(expected = IllegalArgumentException.class)
  public void testDecodeEmailFromTokenNoToken() {
    OAuthUtils.decodeEmailFromToken(mock(IAuthorizationServer.class), "");
    fail("should not decode empty string");
  }

  @Test(expected = MalformedJwtException.class)
  public void testDecodeEmailFromTokenBadToken() {
    OAuthUtils.decodeEmailFromToken(mock(IAuthorizationServer.class), "1234");
    fail("should not decode bad token");
  }

  @Test(expected = UnsupportedJwtException.class)
  public void testDecodeEmailFromTokenNoneAlgHeader() {
    String noneAlgHeader = Base64.getEncoder().encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
    String payload = Base64.getEncoder().encodeToString("payload".getBytes(StandardCharsets.UTF_8));
    OAuthUtils.decodeEmailFromToken(mock(IAuthorizationServer.class), noneAlgHeader + '.' + payload + '.');
    fail("should not decode None Alg header token");
  }

  @Test(expected = MalformedJwtException.class)
  public void testDecodeEmailFromTokenWrongAlgHeader() {
    String wrongAlgHeader = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));
    String payload = Base64.getEncoder().encodeToString("payload".getBytes(StandardCharsets.UTF_8));
    OAuthUtils.decodeEmailFromToken(mock(IAuthorizationServer.class), wrongAlgHeader + '.' + payload + '.');
    fail("should not decode None Alg header token");
  }

  @Test
  public void testGetDeployment() {
    IAuthorizationServer server = mock(IAuthorizationServer.class);
    when(server.getRealm()).thenReturn("redhat-external");
    when(server.getClientId()).thenReturn("vscode-redhat-account");
    when(server.getURL()).thenReturn("https://sso.redhat.com/auth");
    KeycloakDeployment deployment = OAuthUtils.getDeployment(server);
    assertNotNull(deployment);
    assertNotNull(deployment.getPublicKeyLocator());
  }

}