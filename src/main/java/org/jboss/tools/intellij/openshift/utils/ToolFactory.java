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
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCli;

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

    private final Tool<Odo> odo = new Tool<>("odo", OdoCli::new);
    private final Tool<Helm> helm = new Tool<>("helm", HelmCli::new);

    private ToolFactory() {
    }

    public CompletableFuture<Odo> getOdo(Project project) {
        return odo.get(project);
    }

    public void resetOdo() {
        this.odo.reset();
    }

    public CompletableFuture<Helm> getHelm(Project project) {
        return helm.get(project);
    }

    public void resetHelm() {
        this.helm.reset();
    }

    private static class Tool<T> {

        private final String name;

        private final BiFunction<Project, String, T> toolFactory;

        private CompletableFuture<T> factory = null;

        private Tool(String name, BiFunction<Project, String, T> toolFactory) {
            this.name = name;
            this.toolFactory = toolFactory;
        }

        private CompletableFuture<T> get(Project project) {
            if (factory == null) {
                this.factory = create(name, toolFactory, project);
            }
            return factory;
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

        private void reset() {
            this.factory = null;
        }
    }
}
