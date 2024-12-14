/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.ProjectTopics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.utils.KubernetesClientFactory;
import org.jboss.tools.intellij.openshift.utils.ProjectUtils;
import org.jboss.tools.intellij.openshift.utils.ToolFactory;
import org.jboss.tools.intellij.openshift.utils.ToolFactory.Tool;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.oc.Oc;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentDescriptor;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jboss.tools.intellij.openshift.utils.odo.OdoProcessHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationsRootNode
  implements ModuleListener, ConfigWatcher.Listener, ProcessingNode, StructureAwareNode, ParentableNode<ApplicationsRootNode>, Disposable, DumbAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsRootNode.class);
  private final Project project;
  private final ApplicationsTreeStructure structure;
  private final ProcessingNodeImpl processingNode = new ProcessingNodeImpl();
  private final Map<String, ComponentDescriptor> components = new HashMap<>();
  private CompletableFuture<ApplicationRootNodeOdo> odoFuture;
  private CompletableFuture<Tool<Helm>> helmFuture;
  private CompletableFuture<Tool<Oc>> ocFuture;
  private boolean logged;
  private KubernetesClient client;
  private final OdoProcessHelper processHelper;

  public ApplicationsRootNode(Project project, ApplicationsTreeStructure structure, Disposable parent) {
    this.project = project;
    this.structure = structure;
    initConfigWatcher();
    registerProjectListener(project);
    this.processHelper = new OdoProcessHelper();
    Disposer.register(parent, this);
  }

  private static boolean shouldLogMessage(String message) {
    return !(message.contains("Unauthorized") ||
      message.contains("unable to access the cluster: servicebindings.binding.operators.coreos.com") ||
      message.contains("the server has asked for the client to provide credentials") ||
      message.contains("connect: no route to host"));
  }

  public boolean isLogged() {
    return logged;
  }

  public void setLogged(boolean logged) {
    this.logged = logged;
  }

  private CompletableFuture<ApplicationRootNodeOdo> doGetOdo() {
    if (odoFuture == null) {
      this.odoFuture =
        ReadAction.compute(() -> ToolFactory.getInstance()
          .createOdo(getClient(), project)
          .thenApply(tool -> {
            ApplicationRootNodeOdo odo = new ApplicationRootNodeOdo(tool.get(), tool.isDownloaded(), this, processHelper);
            loadProjectModel(odo, project);
            return odo;
          }));
    }
    return odoFuture;
  }

  public CompletableFuture<ApplicationRootNodeOdo> getOdo() {
    return doGetOdo()
      .whenComplete((ApplicationRootNodeOdo odo, Throwable err) -> {
        if (odo != null
          && odo.isDownloaded()) {
          structure.fireModified(this);
        }
      });
  }

  private void disposeClientAwareCLIs() {
    safeCancel(odoFuture);
    this.odoFuture = null;
    safeCancel(ocFuture);
    this.ocFuture = null;
  }

  private void safeCancel(Future<?> future) {
    if (future != null) {
      try {
        future.cancel(true);
      } catch (CompletionException e) {
        // swallowing intentionally
      }
    }
  }

  public CompletableFuture<ToolFactory.Tool<Oc>> getOcTool() {
    if (ocFuture == null) {
      this.ocFuture = ToolFactory.getInstance().createOc(getClient());
    }
    return ocFuture;
  }

  private CompletableFuture<ToolFactory.Tool<Helm>> getHelmTool() {
    if (helmFuture == null) {
      this.helmFuture = ToolFactory.getInstance()
        .createHelm()
        .whenComplete((tool, err) -> {
          if (tool.isDownloaded()) {
            structure.fireModified(this);
          }
        });
    }
    return helmFuture;
  }

  public Helm getHelm() {
    Tool<Helm> tool = getHelmTool().getNow(null);
    if (tool == null) {
      return null;
    }
    return tool.get();
  }

  public Project getProject() {
    return project;
  }

  protected void initConfigWatcher() {
    ExecHelper.submit(new ConfigWatcher(this));
  }

  public Map<String, ComponentDescriptor> getLocalComponents() {
    return components;
  }

  protected void loadProjectModel(OdoFacade odo, Project project) {
    if (odo == null) {
      return;
    }
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      addContext(odo, ProjectUtils.getModuleRoot(module));
    }
  }

  @Override
  public void moduleAdded(@NotNull Project project, @NotNull Module module) {
    addContext(getOdo().getNow(null), ProjectUtils.getModuleRoot(module));
  }

  @Override
  public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
    removeContext(ProjectUtils.getModuleRoot(module));
  }

  private void addContextToSettings(String path, ComponentDescriptor descriptor) {
    if (!components.containsKey(path)) {
      if (descriptor.isPreOdo3()) {
        migrateOdo(descriptor);
      }
      components.put(path, descriptor);
    }
  }

  private void migrateOdo(ComponentDescriptor descriptor) {
    doGetOdo().whenComplete((odo, err) -> {
        if (odo != null) {
          odo.migrateComponent(descriptor.getName());
        }
      })
      .thenRun(() ->
        NotificationUtils.notifyInformation(
          "Component migration",
          "The component " + descriptor.getName() + " has been migrated to odo 3.x"));
  }

  private void addContext(Odo odo, VirtualFile modulePathFile) {
    if (odo == null) {
      return;
    }
    if (modulePathFile != null && modulePathFile.isValid()) {
      try {
        List<ComponentDescriptor> descriptors = odo.discover(modulePathFile.toNioPath().toString());
        descriptors.forEach(descriptor ->
          addContextToSettings(descriptor.getPath(), descriptor)
        );
      } catch (IOException ex) {
        //filter out some common exception when no logged or no authorizations
        if (shouldLogMessage(ex.getMessage())) {
          LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
      }
    }
  }

  public void addContext(String modulePath) {
    addContext(
      getOdo().getNow(null),
      LocalFileSystem.getInstance().refreshAndFindFileByPath(modulePath));
  }

  private void removeContextFromSettings(String modulePath) {
    if (components.containsKey(modulePath)) {
      components.remove(modulePath);
      structure.fireModified(this);
    }
  }

  public void removeContext(File file) {
    if (file.exists()) {
      removeContextFromSettings(file.getPath());
    }
  }

  private void removeContext(VirtualFile modulePathFile) {
    removeContextFromSettings(modulePathFile.getPath());
  }

  protected void registerProjectListener(Project project) {
    MessageBusConnection connection = project.getMessageBus().connect(this);
    connection.subscribe(ProjectTopics.MODULES, this);
  }

  @Override
  public void onUpdate(Config updated) {
    Config current = getClient().getConfiguration();
    if (!ConfigHelper.areEqual(current, updated)) {
      this.client = createClient(updated);
      refresh();
    }
  }

  public synchronized void refresh() {
    disposeClientAwareCLIs();
    doGetOdo().whenComplete((odo, err) ->
      structure.fireModified(ApplicationsRootNode.this)
    );
  }

  @Override
  public ApplicationsTreeStructure getStructure() {
    return structure;
  }

  @Override
  public synchronized void startProcessing(String message) {
    this.processingNode.startProcessing(message);
  }

  @Override
  public synchronized void stopProcessing() {
    this.processingNode.stopProcessing();
  }

  @Override
  public synchronized boolean isProcessing() {
    return processingNode.isProcessing();
  }

  @Override
  public synchronized boolean isProcessingStopped() {
    return processingNode.isProcessingStopped();
  }

  @Override
  public String getMessage() {
    return processingNode.getMessage();
  }

  @Override
  public ApplicationsRootNode getParent() {
    return this;
  }

  @Override
  public ApplicationsRootNode getRoot() {
    return this;
  }

  @Override
  public void dispose() {
    disposeClientAwareCLIs();
  }

  protected KubernetesClient getClient() {
    if (client == null) {
      this.client = createClient(new ConfigBuilder().build());
    }
    return client;
  }

  protected KubernetesClient createClient(Config config) {
    return new KubernetesClientFactory().apply(config);
  }

}
