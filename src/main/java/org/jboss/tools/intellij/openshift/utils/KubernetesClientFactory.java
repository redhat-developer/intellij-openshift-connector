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

import com.redhat.devtools.intellij.common.ssl.IDEATrustManager;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.http.HttpClient;
import io.fabric8.kubernetes.client.internal.SSLUtils;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesClientFactory implements Function<Config, KubernetesClient> {

  private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesClientFactory.class);

  @Override
    public KubernetesClient apply(Config config) {
      return new KubernetesClientBuilder()
        .withConfig(config)
        .withHttpClientBuilderConsumer(builder -> setSslContext(builder, config))
        .build();
    }

    private void setSslContext(HttpClient.Builder builder, Config config) {
      try {
        X509TrustManager externalTrustManager = new IDEATrustManager().configure(List.of(Arrays.stream(SSLUtils.trustManagers(config))
          .filter(X509ExtendedTrustManager.class::isInstance)
          .map(X509ExtendedTrustManager.class::cast).toArray(X509ExtendedTrustManager[]::new)));
        builder.sslContext(SSLUtils.keyManagers(config), List.of(externalTrustManager).toArray(new TrustManager[0]));
      } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException |
               UnrecoverableKeyException | InvalidKeySpecException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
  }
