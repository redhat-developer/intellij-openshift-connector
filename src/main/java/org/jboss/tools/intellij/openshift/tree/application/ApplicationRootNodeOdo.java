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
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OperatorCRD;
import org.jboss.tools.intellij.openshift.utils.odo.Service;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;

public class ApplicationRootNodeOdo implements Odo {
    private final Odo delegate;
    private final ApplicationsRootNode root;
    private final FileOperations fileOperations;

    ApplicationRootNodeOdo(Odo delegate, ApplicationsRootNode root) {
        this(delegate, root, new FileOperations());
    }

    ApplicationRootNodeOdo(Odo delegate, ApplicationsRootNode root, FileOperations fileOperations) {
        this.delegate = delegate;
        this.root = root;
        this.fileOperations = fileOperations;
    }

    @Override
    public List<String> getNamespaces() throws IOException {
        return delegate.getNamespaces();
    }

    @Override
    public String getNamespace() throws IOException {
        return delegate.getNamespace();
    }

    @Override
    public void start(String project, String context, String component, ComponentFeature feature,
                      Consumer<Boolean> callback, Consumer<Boolean> processTerminatedCallback) throws IOException {
        delegate.start(project, context, component, feature, callback, processTerminatedCallback);
    }

    @Override
    public void stop(String project, String context, String component, ComponentFeature feature) throws IOException {
        delegate.stop(project, context, component, feature);
    }

    @Override
    public boolean isStarted(String project, String context, String component, ComponentFeature feature) {
        return delegate.isStarted(project, context, component, feature);
    }

    @Override
    public void describeComponent(String project, String context, String component) throws IOException {
        delegate.describeComponent(project, context, component);
    }

    @Override
    public List<ComponentMetadata> analyze(String path) throws IOException {
        return delegate.analyze(path);
    }

    @Override
    public void createComponent(String project, String componentType, String registryName, String component, String source, String devfile, String starter) throws IOException {
        if (!StringUtil.isEmptyOrSpaces(starter)) {
            File tmpdir = fileOperations.createTempDir("odotmp");
            delegate.createComponent(project, componentType, registryName, component, tmpdir.getAbsolutePath(), devfile, starter);
            File directory = fileOperations.copyTo(tmpdir, source);
            fileOperations.refresh(directory);
        } else {
            delegate.createComponent(project, componentType, registryName, component, source, devfile, starter);
        }
    }

    @Override
    public void createService(String project, ServiceTemplate serviceTemplate, OperatorCRD serviceCRD,
                              String service, ObjectNode spec, boolean wait) throws IOException {
        delegate.createService(project, serviceTemplate, serviceCRD, service, spec, wait);
    }

    @Override
    public String getServiceTemplate(String project, String service) throws IOException {
        return delegate.getServiceTemplate(project, service);
    }

    @Override
    public void deleteService(String project, Service service) throws IOException {
        delegate.deleteService(project, service);
    }

    @Override
    public List<DevfileComponentType> getComponentTypes() throws IOException {
        return delegate.getComponentTypes();
    }

    @Override
    public List<ServiceTemplate> getServiceTemplates() throws IOException {
        return delegate.getServiceTemplates();
    }

    @Override
    public void describeServiceTemplate(String template) throws IOException {
        delegate.describeServiceTemplate(template);
    }

    @Override
    public List<URL> listURLs(String project, String context, String component) throws IOException {
        return delegate.listURLs(project, context, component);
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
        delegate.deleteComponent(project, context, component, kind);
    }

    @Override
    public void follow(String project, String context, String component, boolean deploy, String platform) throws IOException {
        delegate.follow(project, context, component, deploy, platform);
    }

    @Override
    public void log(String project, String context, String component, boolean deploy, String platform) throws IOException {
        delegate.log(project, context, component, deploy, platform);
    }

    @Override
    public boolean isLogRunning(String context, String component, boolean deploy) throws IOException {
        return delegate.isLogRunning(context, component, deploy);
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
    public void login(String url, String userName, char[] password, char[] token) throws IOException {
        delegate.login(url, userName, password, token);
    }

    @Override
    public List<Component> getComponents(String project) throws IOException {
        List<Component> components = delegate.getComponents(project);
        for (Map.Entry<String, ComponentDescriptor> entry : root.getComponents().entrySet()) {
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
    }

    @Override
    public List<Service> getServices(String project) throws IOException {
        return delegate.getServices(project);
    }

    @Override
    public void listComponents() throws IOException {
        delegate.listComponents();
    }

    @Override
    public void listServices() throws IOException {
        delegate.listServices();
    }

    @Override
    public void about() throws IOException {
        delegate.about();
    }

    @Override
    public Binding link(String project, String context, String component, String target) throws IOException {
        return delegate.link(project, context, component, target);
    }

    @Override
    public List<Binding> listBindings(String project, String context, String component) throws IOException {
        return delegate.listBindings(project, context, component);
    }

    @Override
    public void deleteBinding(String project, String context, String component, String binding) throws IOException {
        delegate.deleteBinding(project, context, component, binding);
    }

    @Override
    public void debug(String project, String context, String component, Integer port) throws IOException {
        delegate.debug(project, context, component, port);
    }

    @Override
    public DebugStatus debugStatus(String project, String context, String component) throws IOException {
        return delegate.debugStatus(project, context, component);
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
    public List<DevfileComponentType> getComponentTypes(String name) throws IOException {
        return delegate.getComponentTypes(name);
    }

    @Override
    public boolean isOpenShift() {
        return delegate.isOpenShift();
    }

    @Override
    public void migrateComponent(String context, String name){
        delegate.migrateComponent(context, name);
    }

    @Override
    public void release() {
        delegate.release();
    }

    /** for testing purposes **/
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

}
