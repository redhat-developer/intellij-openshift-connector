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

import com.redhat.devtools.intellij.common.kubernetes.ClusterHelper;
import com.redhat.devtools.intellij.common.kubernetes.ClusterInfo;
import com.redhat.devtools.intellij.common.ssl.IDEATrustManager;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.NetworkUtils;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.http.HttpClient;
import io.fabric8.kubernetes.client.internal.SSLUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.IS_OPENSHIFT;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.KUBERNETES_VERSION;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.OPENSHIFT_VERSION;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.asyncSend;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.instance;

public class Cli {

  private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);

  protected final KubernetesClient client;

  protected Cli(Supplier<KubernetesClient> kubernetesClientFactory) {
    this.client = kubernetesClientFactory.get();
  }

  public static final class KubernetesClientFactory implements Supplier<KubernetesClient> {

    @Override
    public KubernetesClient get() {
      String current = ConfigHelper.getCurrentContextName();
      Config config = Config.autoConfigure(current);
      return new KubernetesClientBuilder().withConfig(config).withHttpClientBuilderConsumer(builder -> setSslContext(builder, config)).build();
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

  public static final class EnvVarFactory implements Function<String, Map<String, String>> {

    @Override
    public Map<String, String> apply(String url) {
      try {
        return NetworkUtils.buildEnvironmentVariables(url);
      } catch (URISyntaxException e) {
        return Collections.emptyMap();
      }
    }
  }

  public static final class TelemetryReport {

    public void report(KubernetesClient client) {
      TelemetryMessageBuilder.ActionMessage telemetry = instance().getBuilder().action(NAME_PREFIX_MISC + "login");
      try {
        ClusterInfo info = ClusterHelper.getClusterInfo(client);
        telemetry.property(KUBERNETES_VERSION, info.getKubernetesVersion());
        telemetry.property(IS_OPENSHIFT, Boolean.toString(info.isOpenshift()));
        telemetry.property(OPENSHIFT_VERSION, info.getOpenshiftVersion());
        asyncSend(telemetry);
      } catch (RuntimeException e) {
        // do not send telemetry when there is no context ( ie default kube URL as master URL )
        asyncSend(telemetry.error(e));
      }
    }

    @NotNull
    public TelemetryConfiguration.ConfigurationChangedListener onTelemetryConfigurationChanged(Map<String, String> envVars) {
      return (String key, String value) -> {
        if (TelemetryConfiguration.KEY_MODE.equals(key)) {
          addTelemetryVars(envVars);
        }
      };
    }

    public void addTelemetryVars(Map<String, String> envVars) {
      if (TelemetryConfiguration.getInstance().isEnabled()) {
        envVars.put("ODO_TRACKING_CONSENT", "yes");
        envVars.put("TELEMETRY_CALLER", "intellij");
      } else {
        envVars.put("ODO_TRACKING_CONSENT", "no");
      }
    }
  }
}
