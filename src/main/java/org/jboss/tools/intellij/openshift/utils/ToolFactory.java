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
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.helm.HelmCli;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCli;
import org.jboss.tools.intellij.openshift.utils.odo.OdoDelegate;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class ToolFactory {


  private static final String TOOLS_JSON = "/tools.json";

  private static ToolFactory INSTANCE;

  public static ToolFactory getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ToolFactory();
    }
    return INSTANCE;
  }

  private final Tool<OdoDelegate> odo = new Tool<>("odo", OdoCli::new);
  private final Tool<Helm> helm = new Tool<>("helm", (project, command) -> new HelmCli(command));

  private ToolFactory() {
  }

  public CompletableFuture<OdoDelegate> createOdo(Project project) {
    return odo.create(project);
  }

  public CompletableFuture<Helm> createHelm(Project project) {
    return helm.create(project);
  }

  private static class Tool<T> {

    private final String name;

    private final BiFunction<Project, String, T> toolFactory;

    private Tool(String name, BiFunction<Project, String, T> toolFactory) {
      this.name = name;
      this.toolFactory = toolFactory;
    }

    private CompletableFuture<T> create(Project project) {
      return create(name, toolFactory, project);
    }

    private CompletableFuture<T> create(String name, BiFunction<Project, String, T> toolFactory, Project project) {
      return DownloadHelper.getInstance()
        .downloadIfRequiredAsync(name, ToolFactory.class.getResource(TOOLS_JSON))
        .thenApply(command -> {
          if (command != null) {
            return toolFactory.apply(project, command);
          } else {
            return null;
          }
        });
    }
  }
}
