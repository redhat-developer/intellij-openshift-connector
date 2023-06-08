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
package org.jboss.tools.intellij.openshift.utils.odo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;

public class OdoProjectDecorator implements Odo {
    private final Odo delegate;
    private final ApplicationsRootNode root;

    public OdoProjectDecorator(Odo delegate, ApplicationsRootNode root) {
        this.delegate = delegate;
        this.root = root;
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
        if (StringUtils.isNotBlank(starter)) {
            File tmpdir;
            if (SystemInfo.isWindows) {
                tmpdir = Files.createTempDirectory("odotmp").toFile();
            } else {
                FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(
                        PosixFilePermissions.fromString("rwxr-x---"));
                tmpdir = Files.createTempDirectory("odotmp", attr).toFile();
            }
            delegate.createComponent(project, componentType, registryName, component, tmpdir.getAbsolutePath(), devfile, starter);
            File sourceDir = new File(source);
            FileUtils.copyDirectory(tmpdir, sourceDir);
            FileUtils.deleteQuietly(tmpdir);
            VirtualFile[] files = new VirtualFile[] {VfsUtil.findFileByIoFile(sourceDir,true)};
            VfsUtil.markDirtyAndRefresh(false, true, true, files);
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
    public ComponentInfo getComponentInfo(String project, String component, String path,
                                          ComponentKind kind) throws IOException {
        return delegate.getComponentInfo(project, component, path, kind);
    }

    @Override
    public void deleteComponent(String project, String context, String component,
                                ComponentKind kind) throws IOException {
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
                Optional<Component> found = components.stream().filter(comp1 -> comp1.getName().equals(componentDescriptor.getName())).findFirst();
            if (found.isPresent()) {
                found.get().setPath(path);
                found.get().setInfo(getComponentInfo(project, componentDescriptor.getName(), path, ComponentKind.DEVFILE));
            } else {
                components.add(Component.of(componentDescriptor.getName(), new ComponentFeatures(), path,
                        getComponentInfo(project, componentDescriptor.getName(), path, ComponentKind.DEVFILE)));
            }
        }
        return components;
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
}
