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
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.helm.HelmCli;
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

  private final Factory<OdoDelegate> odo = new Factory<>("odo", OdoCli::new);
  private final Factory<Helm> helm = new Factory<>("helm", (project, command) -> new HelmCli(command));

  private ToolFactory() {
  }

    public CompletableFuture<Tool<OdoDelegate>> createOdo(Project project) {
        return odo.create(project);
    }

  public CompletableFuture<Tool<Helm>> createHelm(Project project) {
    return helm.create(project);
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

    private static class Factory<T> {

    private final String name;
        private final URL url = ToolFactory.class.getResource(TOOLS_JSON);

    private final BiFunction<Project, String, T> toolFactory;

    private Factory(String name, BiFunction<Project, String, T> toolFactory) {
      this.name = name;
      this.toolFactory = toolFactory;
    }

    private CompletableFuture<Tool<T>> create(Project project) {
      return create(name, toolFactory, project);
    }

    private CompletableFuture<Tool<T>> create(String name, BiFunction<Project, String, T> toolFactory, Project project) {
      return DownloadHelper.getInstance()
        .downloadIfRequiredAsync(name, url)
        .thenApply(toolInstance -> {
          if (toolInstance != null) {
            return new Tool<>(toolFactory.apply(project, toolInstance.getCommand()), toolInstance.isDownloaded());
          } else {
            return null;
          }
        });
    }
  }
}
