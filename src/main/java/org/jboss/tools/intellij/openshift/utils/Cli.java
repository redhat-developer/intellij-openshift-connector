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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.redhat.devtools.intellij.common.kubernetes.ClusterHelper;
import com.redhat.devtools.intellij.common.kubernetes.ClusterInfo;
import com.redhat.devtools.intellij.common.utils.NetworkUtils;
import com.redhat.devtools.intellij.telemetry.core.configuration.TelemetryConfiguration;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.IS_OPENSHIFT;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.KUBERNETES_VERSION;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.OPENSHIFT_VERSION;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.instance;

public class Cli {

  protected final String command;

  protected Cli(String command) {
    this.command = command;
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
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        TelemetryMessageBuilder.ActionMessage telemetry = instance().getBuilder().action(NAME_PREFIX_MISC + "login");
        try {
          ClusterInfo info = ClusterHelper.getClusterInfo(client);
          telemetry
            .property(KUBERNETES_VERSION, info.getKubernetesVersion())
            .property(IS_OPENSHIFT, Boolean.toString(info.isOpenshift()))
            .property(OPENSHIFT_VERSION, info.getOpenshiftVersion())
            .send();
        } catch (RuntimeException e) {
          // do not send telemetry when there is no context ( ie default kube URL as master URL )
          telemetry.error(e).send();
        }
      });
    }

    public void subscribe(MessageBus bus, Map<String, String> envVars) {
      bus.connect().subscribe(
        TelemetryConfiguration.ConfigurationChangedListener.CONFIGURATION_CHANGED,
        onTelemetryConfigurationChanged(envVars)
      );
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
