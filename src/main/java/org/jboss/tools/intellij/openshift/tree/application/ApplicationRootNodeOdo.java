/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.jboss.tools.intellij.openshift.utils.odo.Binding;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentDescriptor;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeature;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeatures;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentKind;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentMetadata;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentTypeInfo;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileRegistry;
import org.jboss.tools.intellij.openshift.utils.odo.OdoDelegate;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jboss.tools.intellij.openshift.utils.odo.OdoProcessHelper;
import org.jboss.tools.intellij.openshift.utils.odo.OperatorCRD;
import org.jboss.tools.intellij.openshift.utils.odo.Service;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ApplicationRootNodeOdo implements OdoFacade {
  private final OdoDelegate delegate;
  private final boolean isDownloaded;
  private final OdoProcessHelper processHelper;
  private final ApplicationsRootNode root;
  private final FileOperations fileOperations;

  public ApplicationRootNodeOdo(OdoDelegate delegate, boolean isDownloaded, ApplicationsRootNode root, OdoProcessHelper processHelper) {
    this(delegate, isDownloaded, processHelper, root, new FileOperations());
  }

  ApplicationRootNodeOdo(OdoDelegate delegate, boolean isDownloaded, OdoProcessHelper processHelper, ApplicationsRootNode root, FileOperations fileOperations) {
    this.delegate = delegate;
    this.isDownloaded = isDownloaded;
    this.processHelper = processHelper;
    this.root = root;
    this.fileOperations = fileOperations;
  }

  @Override
  public List<String> getNamespaces() throws IOException {
    return delegate.getNamespaces();
  }

  @Override
  public String getCurrentNamespace() {
    return delegate.getCurrentNamespace();
  }

  @Override
  public boolean namespaceExists(String name) {
    return delegate.namespaceExists(name);
  }

  @Override
  public String getNamespaceKind() {
    return delegate.getNamespaceKind();
  }

  @Override
  public void start(String context, String component, ComponentFeature feature,
                    @NotNull Consumer<Boolean> callback, Consumer<Boolean> processTerminatedCallback) throws IOException {
    Map<ComponentFeature, ProcessHandler> componentMap = getComponentFeature(component);
    ProcessAdapter processAdapter = new ProcessAdapter() {
      private boolean callBackCalled = false;

      @Override
      public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        //computeIfAbsent() usage here occasionally throws ConcurrentModificationException...
        if (!componentMap.containsKey(feature)) {
          componentMap.put(feature, event.getProcessHandler());
        }
        if (!callBackCalled && event.getText().contains(feature.getOutput())) {
          callback.accept(true);
          callBackCalled = true;
        }
      }

      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        componentMap.remove(feature);
        processTerminatedCallback.accept(true);
      }
    };
    delegate.start(context, feature, componentMap.get(feature), processAdapter);
  }

  @Override
  public void stop(String context, String component, ComponentFeature feature) throws IOException {
    delegate.stop(context, feature, getComponentFeature(component).remove(feature));
  }

  @Override
  public boolean isStarted(String component, ComponentFeature feature) {
    return getComponentFeature(component).containsKey(feature);
  }

  @Override
  public void describeComponent(String context) throws IOException {
    delegate.describeComponent(context);
  }

  @Override
  public List<ComponentMetadata> analyze(String path) throws IOException {
    return delegate.analyze(path);
  }

  @Override
  public void createComponent(String componentType, String registryName, String component, String source, String devfile, String starter) throws IOException {
    if (!StringUtil.isEmptyOrSpaces(starter)) {
      File tmpdir = fileOperations.createTempDir("odotmp");
      delegate.createComponent(componentType, registryName, component, tmpdir.getAbsolutePath(), devfile, starter);
      File directory = fileOperations.copyTo(tmpdir, source);
      fileOperations.refresh(directory);
    } else {
      delegate.createComponent(componentType, registryName, component, source, devfile, starter);
    }
  }

  @Override
  public void createService(String project, ServiceTemplate serviceTemplate, OperatorCRD serviceCRD,
                            String service, ObjectNode spec, boolean wait) throws IOException {
    delegate.createService(project, serviceTemplate, serviceCRD, service, spec, wait);
  }

  @Override
  public void deleteService(String project, Service service) throws IOException {
    delegate.deleteService(project, service);
  }

  @Override
  public List<DevfileComponentType> getAllComponentTypes() throws IOException {
    return delegate.getAllComponentTypes();
  }

  @Override
  public List<ServiceTemplate> getServiceTemplates() throws IOException {
    return delegate.getServiceTemplates();
  }

  @Override
  public List<URL> listURLs(String context) throws IOException {
    return delegate.listURLs(context);
  }

  @Override
  public ComponentInfo getComponentInfo(String project, String component, String path, ComponentKind kind) throws IOException {
    return delegate.getComponentInfo(project, component, path, kind);
  }

  @Override
  public void deleteComponent(String project, String context, String component, ComponentKind kind) throws IOException {
    if (context != null) {
      root.removeContext(new File(context));
    }
    cleanupComponent(component);
    delegate.deleteComponent(project, context, component, kind);
  }

  @Override
  public void follow(String context, String component, boolean deploy, String platform) throws IOException {
    List<ProcessHandler> handlers = processHelper.getComponentLogProcesses().computeIfAbsent(component, name -> Arrays.asList(new ProcessHandler[2]));
    delegate.follow(context, deploy, platform, handlers);
  }

  @Override
  public void log(String context, String component, boolean deploy, String platform) throws IOException {
    List<ProcessHandler> handlers = processHelper.getComponentLogProcesses().computeIfAbsent(component, name -> Arrays.asList(new ProcessHandler[2]));
    delegate.log(context, deploy, platform, handlers);
  }

  @Override
  public boolean isLogRunning(String component, boolean deploy) {
    return processHelper.getComponentLogProcesses().computeIfAbsent(component, name -> Arrays.asList(new ProcessHandler[2])).get(deploy ? 1 : 0) != null;
  }

  @Override
  public void createProject(String project) throws IOException {
    delegate.createProject(project);
  }

  @Override
  public void deleteProject(String project) throws IOException {
    delegate.deleteProject(project);
  }

  @Override
  public void setProject(String project) throws IOException {
    delegate.setProject(project);
  }

  @Override
  public void login(String url, String userName, char[] password, char[] token) throws IOException {
    delegate.login(url, userName, password, token);
  }

  @Override
  public boolean isAuthorized() {
    return delegate.isAuthorized();
  }

  @Override
  public List<Component> getComponents(String project) throws IOException {
    List<Component> components = delegate.getComponentsOnCluster(project);
    for (Map.Entry<String, ComponentDescriptor> entry : root.getLocalComponents().entrySet()) {
      String path = entry.getKey();
      ComponentDescriptor componentDescriptor = entry.getValue();
      Optional<Component> found = components.stream()
        .filter(comp1 -> comp1.getName().equals(componentDescriptor.getName()))
        .findFirst();
      if (found.isPresent()) {
        updateComponent(project, path, componentDescriptor, found.get());
      } else {
        components.add(createComponent(project, componentDescriptor, path));
      }
    }
    return components;
  }

  @NotNull
  private Component createComponent(String project, ComponentDescriptor componentDescriptor, String path) throws IOException {
    ComponentInfo info = getComponentInfo(project, componentDescriptor.getName(), path, ComponentKind.DEVFILE);
    return Component.of(
      componentDescriptor.getName(),
      componentDescriptor.getManagedBy(),
      new ComponentFeatures(),
      path,
      info);
  }

  private void updateComponent(String project, String path, ComponentDescriptor componentDescriptor, Component component) throws IOException {
    component.setPath(path);
    ComponentInfo info = getComponentInfo(project, componentDescriptor.getName(), path, ComponentKind.DEVFILE);
    component.setInfo(info);
    Map<ComponentFeature, ProcessHandler> componentMap = getComponentFeature(component.getName());
    if (!componentMap.isEmpty()) {
      componentMap.keySet().forEach(componentFeature -> component.getLiveFeatures().addFeature(componentFeature));
    }
  }

  @Override
  public List<Service> getServices(String project) throws IOException {
    return delegate.getServices(project);
  }

  @Override
  public void about() throws IOException {
    delegate.about();
  }

  @Override
  public Binding link(String context, String target) throws IOException {
    return delegate.link(context, target);
  }

  @Override
  public List<Binding> listBindings(String context) throws IOException {
    return delegate.listBindings(context);
  }

  @Override
  public void deleteBinding(String context, String binding) throws IOException {
    delegate.deleteBinding(context, binding);
  }

  @Override
  public void debug(String context, Integer port) throws IOException {
    delegate.debug(context, port);
  }

  @Override
  public java.net.URL getMasterUrl() {
    return delegate.getMasterUrl();
  }

  @Override
  public List<ComponentDescriptor> discover(String path) throws IOException {
    return delegate.discover(path);
  }

  @Override
  public ComponentTypeInfo getComponentTypeInfo(String componentType, String registryName) throws IOException {
    return delegate.getComponentTypeInfo(componentType, registryName);
  }

  @Override
  public String consoleURL() throws IOException {
    return delegate.consoleURL();
  }

  @Override
  public List<DevfileRegistry> listDevfileRegistries() throws IOException {
    return delegate.listDevfileRegistries();
  }

  @Override
  public void createDevfileRegistry(String name, String url, String token) throws IOException {
    delegate.createDevfileRegistry(name, url, token);
  }

  @Override
  public void deleteDevfileRegistry(String name) throws IOException {
    delegate.deleteDevfileRegistry(name);
  }

  @Override
  public List<DevfileComponentType> getComponentTypesFromRegistry(String name) throws IOException {
    return delegate.getComponentTypesFromRegistry(name);
  }

  @Override
  public boolean isOpenShift() {
    return delegate.isOpenShift();
  }

  @Override
  public void migrateComponent(String name) {
    delegate.migrateComponent(name);
  }

  public boolean isDownloaded() {
    return isDownloaded;
  }

  /**
   * for testing purposes
   **/
  protected static class FileOperations {
    protected File createTempDir(String prefix) throws IOException {
      return FileUtil.createTempDirectory(prefix, null);
    }

    public File copyTo(File source, String destination) throws IOException {
      File destinationDir = new File(destination);
      FileUtils.copyDirectory(source, destinationDir);
      org.apache.commons.io.FileUtils.deleteQuietly(source);
      return destinationDir;
    }


    public void refresh(File file) {
      VirtualFile[] files = new VirtualFile[]{VfsUtil.findFileByIoFile(file, true)};
      VfsUtil.markDirtyAndRefresh(false, true, true, files);
    }
  }

  /**
   * Stop all running processes for a component
   *
   * @param component the component name
   */
  private void cleanupComponent(String component) {
    Map<ComponentFeature, ProcessHandler> featureHandlers = processHelper.getComponentFeatureProcesses().remove(component);
    if (featureHandlers != null) {
      featureHandlers.forEach((feat, handler) -> handler.destroyProcess());
    }
    List<ProcessHandler> logHandlers = processHelper.getComponentLogProcesses().remove(component);
    if (logHandlers != null) {
      logHandlers.stream().filter(Objects::nonNull).forEach(ProcessHandler::destroyProcess);
    }
  }

  private Map<ComponentFeature, ProcessHandler> getComponentFeature(String component) {
    return processHelper.getComponentFeatureProcesses().computeIfAbsent(component, name -> new HashMap<>());
  }
}
