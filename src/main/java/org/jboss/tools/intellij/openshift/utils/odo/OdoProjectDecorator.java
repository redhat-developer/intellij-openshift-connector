package org.jboss.tools.intellij.openshift.utils.odo;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.openshift.client.OpenShiftClient;
import me.snowdrop.servicecatalog.api.model.ServiceInstance;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class OdoProjectDecorator implements Odo {
    private final Odo delegate;
    private final ApplicationTreeModel model;

    public OdoProjectDecorator(Odo delegate, ApplicationTreeModel model) {
        this.delegate = delegate;
        this.model = model;
    }

    @Override
    public List<io.fabric8.openshift.api.model.Project> getProjects(OpenShiftClient client) {
        return delegate.getProjects(client);
    }

    @Override
    public void createApplication(String project, String application) throws IOException {
        delegate.createApplication(project, application);
    }

    @Override
    public void describeApplication(String project, String application) throws IOException {
        delegate.describeApplication(project, application);
    }

    @Override
    public void deleteApplication(String project, String application) throws IOException {
        delegate.deleteApplication(project, application);
    }

    @Override
    public void push(String project, String application, String component) throws IOException {
        delegate.push(project, application, component);
    }

    @Override
    public void describeComponent(String project, String application, String component) throws IOException {
        delegate.describeComponent(project, application, component);
    }

    @Override
    public void watch(String project, String application, String component) throws IOException {
        delegate.watch(project, application, component);
    }

    @Override
    public void createComponentLocal(String project, String application, String componentType, String componentVersion, String component, String source) throws IOException {
        delegate.createComponentLocal(project, application, componentType, componentVersion, component, source);

    }

    @Override
    public void createComponentGit(String project, String application, String componentType, String componentVersion, String component, String source) throws IOException {
        delegate.createComponentGit(project, application, componentType, componentVersion, component, source);
    }

    @Override
    public void createService(String project, String application, String serviceTemplate, String servicePlan, String service) throws IOException {
        delegate.createService(project, application, serviceTemplate, servicePlan, service);
    }

    @Override
    public String getServiceTemplate(OpenShiftClient client, String project, String application, String service) {
        return delegate.getServiceTemplate(client, project, application, service);
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
    public List<Integer> getServicePorts(OpenShiftClient client, String project, String application, String component) {
        return delegate.getServicePorts(client, project, application, component);
    }

    @Override
    public List<org.jboss.tools.intellij.openshift.utils.odo.URL> listURLs(String project, String application, String component) throws IOException {
        return delegate.listURLs(project, application, component);
    }

    @Override
    public void createURL(String project, String application, String component, String name, Integer port) throws IOException {
        createURL(project, application, component, name, port);
    }

    @Override
    public void deleteURL(String project, String application, String component, String name) throws IOException {
        delegate.deleteURL(project, application, component, name);
    }

    @Override
    public void deleteComponent(String project, String application, String component) throws IOException {
        delegate.deleteComponent(project, application, component);
    }

    @Override
    public void follow(String project, String application, String component) throws IOException {
        delegate.follow(project, application, component);
    }

    @Override
    public void log(String project, String application, String component) throws IOException {
        delegate.log(project, application, component);
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
        model.getSettings().forEach((path, component) -> {
           if (component.getProject().equals(project) && applications.stream().noneMatch(application -> application.getName().equals(component.getApplication()))) {
               applications.add(Application.of(component.getApplication()));
           }
        });
        return applications;
    }

    @Override
    public List<Component> getComponents(OpenShiftClient client, String project, String application) {
        List<Component> components = delegate.getComponents(client, project, application);
        model.getSettings().forEach((path, comp) -> {
            if (comp.getProject().equals(project) && comp.getApplication().equals(application)) {
                Optional<Component> found = components.stream().filter(comp1 -> comp1.getName().equals(comp.getName())).findFirst();
                if (found.isPresent()) {
                    found.get().setState(ComponentState.PUSHED);
                    found.get().setPath(path);
                } else {
                    components.add(Component.of(comp.getName(), ComponentState.NOT_PUSHED, path));
                }
            }
        });
        return components;
    }

    @Override
    public List<ServiceInstance> getServices(OpenShiftClient client, String project, String application) {
        return delegate.getServices(client, project, application);
    }

    @Override
    public List<PersistentVolumeClaim> getStorages(OpenShiftClient client, String project, String application, String component) {
        return delegate.getStorages(client, project, application, component);
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
    public void createStorage(String project, String application, String component, String name, String mountPath, String storageSize) throws IOException {
        delegate.createStorage(project, application, component, name, mountPath, storageSize);
    }

    @Override
    public void deleteStorage(String project, String application, String component, String storage) throws IOException {
        delegate.deleteStorage(project, application, component, storage);
    }

    @Override
    public void link(String project, String application, String component, String source, Integer port) throws IOException {
        delegate.link(project, application, component, source, port);
    }
}
