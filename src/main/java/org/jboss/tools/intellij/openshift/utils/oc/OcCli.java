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
import io.fabric8.kubernetes.client.KubernetesClient;
import org.jboss.tools.intellij.openshift.utils.Cli;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jboss.tools.intellij.openshift.Constants.HOME_FOLDER;

public class OcCli extends Cli implements Oc {

  private final String command;
  private final Map<String, String> envVars;

  public OcCli(String command) {
    this(command,
      ApplicationManager.getApplication().getMessageBus(),
      new Cli.KubernetesClientFactory(),
      new Cli.EnvVarFactory(),
      new Cli.TelemetryReport());
  }

  public OcCli(
    String command,
    MessageBus bus,
    Supplier<KubernetesClient> kubernetesClientFactory,
    Function<String, Map<String, String>> envVarFactory,
    Cli.TelemetryReport telemetryReport) {
    super(kubernetesClientFactory);
    this.command = command;
    this.envVars = envVarFactory.apply(String.valueOf(client.getMasterUrl()));
    initTelemetry(telemetryReport, bus);
  }

  private void initTelemetry(TelemetryReport telemetryReport, MessageBus bus) {
    telemetryReport.addTelemetryVars(envVars);
    telemetryReport.subscribe(bus, envVars);
    telemetryReport.report(client);
  }


  private static void execute(@NotNull File workingDirectory, String command, Map<String, String> envs, String... args) throws IOException {
    ExecHelper.executeWithResult(command, true, workingDirectory, envs, args);
  }

  @Override
  public void login(String url, String userName, char[] password, char[] token) throws IOException {
    if (userName != null && !userName.isEmpty()) {
      execute(new File(HOME_FOLDER), command, envVars, "login", url, "-u", userName, "-p", String.valueOf(password), "--insecure-skip-tls-verify");
    } else {
      execute(new File(HOME_FOLDER), command, envVars, "login", url, "--token", String.valueOf(token), "--insecure-skip-tls-verify");
    }
  }

  @Override
  public URL getMasterUrl() {
    return client.getMasterUrl();
  }
}
