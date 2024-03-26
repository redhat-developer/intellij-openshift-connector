/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.oauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.jboss.tools.intellij.openshift.oauth.model.IAuthorizationServer;
import org.jetbrains.annotations.NotNull;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;

public class OAuthUtils {

  private OAuthUtils() {
  }

  /**
   * Extract the email from the OAuth access token.
   *
   * @param token the token
   * @return the email address
   */
  public static String decodeEmailFromToken(IAuthorizationServer server, @NotNull String token) {
    Jws<Claims> claims =
      Jwts.parser()
        .keyLocator(header
          -> getDeployment(server).getPublicKeyLocator().getPublicKey((String) header.get("kid"), getDeployment(server)))
        .build()
        .parseSignedClaims(token);
    return claims.getPayload().get("email", String.class);
  }

  public static KeycloakDeployment getDeployment(IAuthorizationServer server) {
    AdapterConfig config = new AdapterConfig();
    config.setPublicClient(true);
    config.setRealm(server.getRealm());
    config.setResource(server.getClientId());
    config.setAuthServerUrl(server.getURL());
    config.setPkce(true);
    return KeycloakDeploymentBuilder.build(config);
  }
}
