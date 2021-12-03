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
    public void describeApplication(String project, String application) throws IOException {
        delegate.describeApplication(project, application);
    }

    @Override
    public void deleteApplication(String project, String application) throws IOException {
        final IOException[] exception = {null};
        getComponents(project, application).forEach(component -> {
            try {
                deleteComponent(project, application, component.getPath(), component.getName());
            } catch (IOException e) {
                exception[0] = e;
            }
        });
        if (exception[0] != null) {
            throw exception[0];
        }
    }

    @Override
    public void push(String project, String application, String context, String component) throws IOException {
        delegate.push(project, application, context, component);
    }

    @Override
    public void pushWithDebug(String project, String application, String context, String component) throws IOException {
        delegate.pushWithDebug(project, application, context, component);
    }

    @Override
    public void describeComponent(String project, String application, String context, String component) throws IOException {
        delegate.describeComponent(project, application, context, component);
    }

    @Override
    public void watch(String project, String application, String context, String component) throws IOException {
        delegate.watch(project, application, context, component);
    }

    @Override
    public void createComponent(String project, String application, String componentType, String registryName, String component, String source, String devfile, String starter, boolean push) throws IOException {
        if (StringUtils.isNotBlank(starter)) {
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---"));
            File tmpdir = Files.createTempDirectory("odotmp", attr).toFile();
            delegate.createComponent(project, application, componentType, registryName, component, tmpdir.getAbsolutePath(), devfile, starter, push);
            File sourceDir = new File(source);
            FileUtils.copyDirectory(tmpdir, sourceDir);
            FileUtils.deleteQuietly(tmpdir);
            VirtualFile[] files = new VirtualFile[] {VfsUtil.findFileByIoFile(sourceDir,true)};
            VfsUtil.markDirtyAndRefresh(false, true, true, files);
        } else {
            delegate.createComponent(project, application, componentType, registryName, component, source, devfile, starter, push);
        }
    }

    @Override
    public void createService(String project, String application, ServiceTemplate serviceTemplate, OperatorCRD serviceCRD,
                              String service, ObjectNode spec, boolean wait) throws IOException {
        delegate.createService(project, application, serviceTemplate, serviceCRD, service, spec, wait);
    }

    @Override
    public String getServiceTemplate(String project, String application, String service) throws IOException {
        return delegate.getServiceTemplate(project, application, service);
    }

    @Override
    public void deleteService(String project, String application, Service service) throws IOException {
        delegate.deleteService(project, application, service);
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
    public List<URL> listURLs(String project, String application, String context, String component) throws IOException {
        return delegate.listURLs(project, application, context, component);
    }

    @Override
    public ComponentInfo getComponentInfo(String project, String application, String component, String path) throws IOException {
        return delegate.getComponentInfo(project, application, component, path);
    }

    @Override
    public void createURL(String project, String application, String context, String component, String name,
                          Integer port, boolean secure, String host) throws IOException {
        delegate.createURL(project, application, context, component, name, port, secure, host);
    }

    @Override
    public void deleteURL(String project, String application, String context, String component, String name) throws IOException {
        delegate.deleteURL(project, application, context, component, name);
    }

    @Override
    public void undeployComponent(String project, String application, String context, String component) throws IOException {
        delegate.undeployComponent(project, application, context, component);
    }

    @Override
    public void deleteComponent(String project, String application, String context, String component) throws IOException {
        if (context != null) {
            root.removeContext(new File(context));
        }
        delegate.deleteComponent(project, application, context, component);
    }

    @Override
    public void follow(String project, String application, String context, String component) throws IOException {
        delegate.follow(project, application, context, component);
    }

    @Override
    public void log(String project, String application, String context, String component) throws IOException {
        delegate.log(project, application, context, component);
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
    public void logout() throws IOException {
        delegate.logout();
    }

    @Override
    public List<Application> getApplications(String project) throws IOException {
        List<Application> applications = delegate.getApplications(project);
        root.getComponents().forEach((path, component) -> {
            if (component.getProject().equals(project) && applications.stream().noneMatch(application -> application.getName().equals(component.getApplication()))) {
                applications.add(Application.of(component.getApplication()));
            }
        });
        return applications;
    }

    @Override
    public List<Component> getComponents(String project, String application) throws IOException {
        List<Component> components = delegate.getComponents(project, application);
        for (Map.Entry<String, ComponentDescriptor> entry : root.getComponents().entrySet()) {
            String path = entry.getKey();
            ComponentDescriptor comp = entry.getValue();
            if (comp.getProject().equals(project) && comp.getApplication().equals(application)) {
                Optional<Component> found = components.stream().filter(comp1 -> comp1.getName().equals(comp.getName())).findFirst();
                if (found.isPresent()) {
                    found.get().setState(ComponentState.PUSHED);
                    found.get().setPath(path);
                } else {
                    components.add(Component.of(comp.getName(), ComponentState.NOT_PUSHED, path,
                            getComponentInfo(project, application, comp.getName(), path)));
                }
            }
        }
        return components;
    }

    @Override
    public List<Service> getServices(String project, String application) throws IOException {
        return delegate.getServices(project, application);
    }

    @Override
    public List<Storage> getStorages(String project, String application, String context, String component) throws IOException {
        return delegate.getStorages(project, application, context, component);
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
    public void createStorage(String project, String application, String context, String component, String name, String mountPath, String storageSize) throws IOException {
        delegate.createStorage(project, application, context, component, name, mountPath, storageSize);
    }

    @Override
    public void deleteStorage(String project, String application, String context, String component, String storage) throws IOException {
        delegate.deleteStorage(project, application, context, component, storage);
    }

    @Override
    public void link(String project, String application, String context, String component, String target) throws IOException {
        delegate.link(project, application, context, component, target);
    }

    @Override
    public void debug(String project, String application, String context, String component, Integer port) throws IOException {
        delegate.debug(project, application, context, component, port);
    }

    @Override
    public DebugStatus debugStatus(String project, String application, String context, String component) throws IOException {
        return delegate.debugStatus(project, application, context, component);
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
    public boolean isOpenshift() {
        return delegate.isOpenshift();
    }
}
