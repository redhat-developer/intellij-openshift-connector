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
package org.jboss.tools.intellij.openshift.utils.oc;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import org.jboss.tools.intellij.openshift.utils.Cli;
import org.jboss.tools.intellij.openshift.utils.ClientAwareCli;
import org.jetbrains.annotations.NotNull;

import static org.jboss.tools.intellij.openshift.Constants.HOME_FOLDER;

public class OcCli extends ClientAwareCli implements Oc {

  private final Map<String, String> envVars;

  public OcCli(String command, KubernetesClient client) {
    this(command,
      client,
      ApplicationManager.getApplication().getMessageBus(),
      new Cli.EnvVarFactory(),
      new Cli.TelemetryReport());
  }

  public OcCli(
    String command,
    KubernetesClient client,
    MessageBus bus,
    Function<String, Map<String, String>> envVarFactory,
    Cli.TelemetryReport telemetryReport) {
    super(command, client);
    this.envVars = envVarFactory.apply(String.valueOf(client.getMasterUrl()));
    initTelemetry(telemetryReport, bus);
  }

  private void initTelemetry(Cli.TelemetryReport telemetryReport, MessageBus bus) {
    telemetryReport.addTelemetryVars(envVars);
    telemetryReport.subscribe(bus, envVars);
    telemetryReport.report(client);
  }

  private static void execute(@NotNull File workingDirectory, String command, Map<String, String> envs, String... args) throws IOException {
    ExecHelper.executeWithResult(command, true, workingDirectory, envs, args);
  }

  @Override
  public void login(String url, String userName, char[] password, char[] token) throws IOException {
    var args = new ArrayList<String>();
    args.add("login");
    args.add(url);
    args.add("--insecure-skip-tls-verify");
    addKubeconfigParameter(args);
    addLoginParameter(userName, password, token, args);
    execute(new File(HOME_FOLDER), command, envVars, args.toArray(new String[0]));
  }

  private void addKubeconfigParameter(ArrayList<String> args) {
    String kubeconfig = Config.getKubeconfigFilenames().stream()
      .findFirst()
      .orElse(null);
    if (kubeconfig != null) {
      args.add("--kubeconfig");
      args.add(kubeconfig);
    }
  }

  private static void addLoginParameter(String userName, char[] password, char[] token, ArrayList<String> args) {
    if (userName != null && !userName.isEmpty()) {
      args.addAll(Arrays.asList("-u", userName, "-p", String.valueOf(password)));
    } else {
      args.addAll(Arrays.asList("--token", String.valueOf(token)));
    }
  }

  @Override
  public URL getMasterUrl() {
    return client.getMasterUrl();
  }
}
