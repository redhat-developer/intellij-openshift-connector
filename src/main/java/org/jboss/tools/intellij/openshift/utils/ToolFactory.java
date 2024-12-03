/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.DownloadHelper;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.helm.HelmCli;
import org.jboss.tools.intellij.openshift.utils.oc.Oc;
import org.jboss.tools.intellij.openshift.utils.oc.OcCli;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCli;
import org.jboss.tools.intellij.openshift.utils.odo.OdoDelegate;

public class ToolFactory {

  private static final String TOOLS_JSON = "/tools.json";

  private static ToolFactory INSTANCE;

  public static ToolFactory getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ToolFactory();
    }
    return INSTANCE;
  }

  private ToolFactory() {}

  public CompletableFuture<Tool<OdoDelegate>> createOdo(KubernetesClient client, Project project) {
    return new Downloadable<OdoDelegate>("odo")
      .downloadAndCreate(commandName -> new OdoCli(commandName, project, client));
  }

  public CompletableFuture<Tool<Helm>> createHelm() {
    return new Downloadable<Helm>("helm")
      .downloadAndCreate(commandName -> new HelmCli(commandName));
  }

  public CompletableFuture<Tool<Oc>> createOc(KubernetesClient client) {
    return new Downloadable<Oc>("helm")
      .downloadAndCreate(commandName -> new OcCli(commandName, client));
  }

  public static class Tool<T> {
    private final T tool;
    private final boolean isDownloaded;

    private Tool(T tool, boolean isDownloaded) {
      this.tool = tool;
      this.isDownloaded = isDownloaded;
    }

    public T get() {
      return tool;
    }

    public boolean isDownloaded() {
      return isDownloaded;
    }
  }

  private static class Downloadable<T> {

    private final String name;

    private final URL url = ToolFactory.class.getResource(TOOLS_JSON);

    private Downloadable(String name) {
      this.name = name;
    }

    private CompletableFuture<Tool<T>> downloadAndCreate(Function<String, T> factory) {
      return DownloadHelper.getInstance()
        .downloadIfRequiredAsync(name, url)
        .thenApply(toolInstance -> {
          if (toolInstance != null) {
            return new Tool<>(factory.apply(toolInstance.getCommand()), toolInstance.isDownloaded());
          } else {
            return null;
          }
        });
    }
  }
}
