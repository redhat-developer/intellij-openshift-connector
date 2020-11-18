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

import io.fabric8.openshift.api.model.Project;
import io.fabric8.servicecatalog.api.model.ServiceInstance;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;

public class OdoProjectDecorator implements Odo {
    private final Odo delegate;
    private final ApplicationTreeModel model;

    public OdoProjectDecorator(Odo delegate, ApplicationTreeModel model) {
        this.delegate = delegate;
        this.model = model;
    }

    @Override
    public List<io.fabric8.openshift.api.model.Project> getProjects() {
        return delegate.getProjects();
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
                deleteComponent(project, application, component.getPath(), component.getName(), component.getInfo().getComponentKind());
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
    public void describeComponent(String project, String application, String context, String component) throws IOException {
        delegate.describeComponent(project, application, context, component);
    }

    @Override
    public void watch(String project, String application, String context, String component) throws IOException {
        delegate.watch(project, application, context, component);
    }

    @Override
    public void createComponentLocal(String project, String application, String componentType, String componentVersion, String component, String source, boolean push) throws IOException {
        delegate.createComponentLocal(project, application, componentType, componentVersion, component, source, push);
    }

    @Override
    public void createComponentGit(String project, String application, String context, String componentType, String componentVersion, String component, String source, String reference, boolean push) throws IOException {
        delegate.createComponentGit(project, application, context, componentType, componentVersion, component, source, reference, push);
    }

    @Override
    public void createComponentBinary(String project, String application, String context, String componentType, String componentVersion, String component, String source, boolean push) throws IOException {
        delegate.createComponentBinary(project, application, context, componentType, componentVersion, component, source, push);
    }

    @Override
    public void createService(String project, String application, String serviceTemplate, String servicePlan, String service, boolean wait) throws IOException {
        delegate.createService(project, application, serviceTemplate, servicePlan, service, wait);
    }

    @Override
    public String getServiceTemplate(String project, String application, String service) {
        return delegate.getServiceTemplate(project, application, service);
    }

    @Override
    public void deleteService(String project, String application, String service) throws IOException {
        delegate.deleteService(project, application, service);
    }

    @Override
    public List<ComponentType> getComponentTypes() throws IOException {
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
    public List<Integer> getServicePorts(String project, String application, String component) {
        List<Integer> ports = delegate.getServicePorts(project, application, component);
        model.getComponents().forEach((path, comp) -> {
            if (comp.getProject().equals(project) && comp.getApplication().equals(application) && comp.getName().equals(component)) {
                comp.getPorts().forEach(port -> {
                    if (!ports.contains(port)) {
                        ports.add(port);
                    }
                });
            }
        });
        return ports;
    }

    @Override
    public List<URL> listURLs(String project, String application, String context, String component) throws IOException {
        return delegate.listURLs(project, application, context, component);
    }

    @Override
    public ComponentInfo getComponentInfo(String project, String application, String component, ComponentKind kind) throws IOException {
        return delegate.getComponentInfo(project, application, component, kind);
    }

    @Override
    public void createURL(String project, String application, String context, String component, String name,
                          Integer port, boolean secure) throws IOException {
        delegate.createURL(project, application, context, component, name, port, secure);
    }

    @Override
    public void deleteURL(String project, String application, String context, String component, String name) throws IOException {
        delegate.deleteURL(project, application, context, component, name);
    }

    @Override
    public void undeployComponent(String project, String application, String context, String component, ComponentKind kind) throws IOException {
        delegate.undeployComponent(project, application, context, component, kind);
    }

    @Override
    public void deleteComponent(String project, String application, String context, String component, ComponentKind kind) throws IOException {
        delegate.deleteComponent(project, application, context, component, kind);
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
    public void login(String url, String userName, char[] password, String token) throws IOException {
        delegate.login(url, userName, password, token);
    }

    @Override
    public void logout() throws IOException {
        delegate.logout();
    }

    @Override
    public List<Application> getApplications(String project) throws IOException {
        List<Application> applications = delegate.getApplications(project);
        model.getComponents().forEach((path, component) -> {
            if (component.getProject().equals(project) && applications.stream().noneMatch(application -> application.getName().equals(component.getApplication()))) {
                applications.add(Application.of(component.getApplication()));
            }
        });
        return applications;
    }

    @Override
    public List<Component> getComponents(String project, String application) throws IOException {
        List<Component> components = delegate.getComponents(project, application);
        for (Map.Entry<String, ComponentDescriptor> entry : model.getComponents().entrySet()) {
            String path = entry.getKey();
            ComponentDescriptor comp = entry.getValue();
            if (comp.getProject().equals(project) && comp.getApplication().equals(application)) {
                Optional<Component> found = components.stream().filter(comp1 -> comp1.getName().equals(comp.getName())).findFirst();
                if (found.isPresent()) {
                    found.get().setState(ComponentState.PUSHED);
                    found.get().setPath(path);
                } else {
                    try {
                        ComponentKind kind = getComponentKind(path);
                        ComponentInfo.Builder builder = new ComponentInfo.Builder();
                        ComponentInfo info = builder.withComponentKind(kind).build();
                        components.add(Component.of(comp.getName(), ComponentState.NOT_PUSHED, path, info));
                    } catch (IOException e) {
                       throw e;
                    }
                }
            }
        }
        return components;
    }

    @Override
    public List<ServiceInstance> getServices(String project, String application) {
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
    public void link(String project, String application, String component, String context, String source, Integer port) throws IOException {
        delegate.link(project, application, component, context, source, port);
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
    public boolean isServiceCatalogAvailable() {
        return delegate.isServiceCatalogAvailable();
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
    public ComponentKind getComponentKind(String context) throws IOException {
        return delegate.getComponentKind(context);
    }

    @Override
    public List<Project> getPreOdo10Projects() {
        return delegate.getPreOdo10Projects();
    }

    @Override
    public List<Exception> migrateProjects(List<Project> projects, BiConsumer<String, String> reporter) {
        return delegate.migrateProjects(projects, reporter);
    }

    @Override
    public String consoleURL() throws IOException {
        return delegate.consoleURL();
    }
}
