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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.DoneableService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimFluent;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretFluent;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceFluent;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigFluent;
import io.fabric8.openshift.api.model.BuildConfigList;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigFluent;
import io.fabric8.openshift.api.model.DeploymentConfigList;
import io.fabric8.openshift.api.model.DoneableBuildConfig;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.api.model.DoneableImageStream;
import io.fabric8.openshift.api.model.DoneableRoute;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamFluent;
import io.fabric8.openshift.api.model.ImageStreamList;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteFluent;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.BuildConfigResource;
import io.fabric8.openshift.client.dsl.DeployableScalableResource;
import io.fabric8.servicecatalog.api.model.DoneableServiceInstance;
import io.fabric8.servicecatalog.api.model.ServiceInstance;
import io.fabric8.servicecatalog.api.model.ServiceInstanceFluent;
import io.fabric8.servicecatalog.api.model.ServiceInstanceList;
import io.fabric8.servicecatalog.client.ServiceCatalogClient;
import io.fabric8.servicecatalog.client.internal.ServiceInstanceResource;
import org.apache.commons.lang3.StringUtils;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;
import static org.jboss.tools.intellij.openshift.Constants.HOME_FOLDER;
import static org.jboss.tools.intellij.openshift.Constants.OCP3_CONFIG_NAMESPACE;
import static org.jboss.tools.intellij.openshift.Constants.OCP3_WEBCONSOLE_CONFIG_MAP_NAME;
import static org.jboss.tools.intellij.openshift.Constants.OCP3_WEBCONSOLE_YAML_FILE_NAME;
import static org.jboss.tools.intellij.openshift.Constants.OCP4_CONFIG_NAMESPACE;
import static org.jboss.tools.intellij.openshift.Constants.OCP4_CONSOLE_PUBLIC_CONFIG_MAP_NAME;
import static org.jboss.tools.intellij.openshift.Constants.OCP4_CONSOLE_URL_KEY_NAME;
import static org.jboss.tools.intellij.openshift.Constants.PLUGIN_FOLDER;
import static org.jboss.tools.intellij.openshift.KubernetesLabels.APP_LABEL;
import static org.jboss.tools.intellij.openshift.KubernetesLabels.COMPONENT_NAME_LABEL;
import static org.jboss.tools.intellij.openshift.KubernetesLabels.COMPONENT_SOURCE_TYPE_ANNOTATION;
import static org.jboss.tools.intellij.openshift.KubernetesLabels.NAME_LABEL;
import static org.jboss.tools.intellij.openshift.KubernetesLabels.ODO_MIGRATED_LABEL;
import static org.jboss.tools.intellij.openshift.KubernetesLabels.RUNTIME_NAME_LABEL;
import static org.jboss.tools.intellij.openshift.KubernetesLabels.RUNTIME_VERSION_LABEL;
import static org.jboss.tools.intellij.openshift.KubernetesLabels.VCS_URI_ANNOTATION;

public class OdoCli implements Odo {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdoCli.class);

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    private final String command;

    private final OpenShiftClient client;

    private Map<String, String> envVars;

    OdoCli(String command) {
        this.command = command;
        this.client = new DefaultOpenShiftClient(new ConfigBuilder().build());
        try {
            this.envVars = NetworkUtils.buildEnvironmentVariables(this.getMasterUrl().toString());
        } catch (URISyntaxException e) {
            this.envVars = Collections.emptyMap();
        }
    }

    private ObjectMapper configureObjectMapper(final StdNodeBasedDeserializer<? extends List<?>> deserializer) {
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(List.class, deserializer);
        return JSON_MAPPER.registerModule(module);
    }

    @Override
    public List<Project> getProjects() {
        return client.projects().list().getItems();
    }

    private static String execute(File workingDirectory, String command, Map<String, String> envs, String... args) throws IOException {
        String output = ExecHelper.execute(command, workingDirectory, envs, args);
        try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
            BinaryOperator<String> reducer = new BinaryOperator<String>() {
                private boolean notificationFound = false;

                @Override
                public String apply(String s, String s2) {
                    if (s2.startsWith("---")) {
                        notificationFound = true;
                    }
                    return notificationFound ? s : s + s2 + "\n";
                }
            };
            return reader.lines().reduce("", reducer);
        }
    }

    private static String execute(String command, Map<String, String> envs, String... args) throws IOException {
        return execute(new File(HOME_FOLDER), command, envs, args);
    }

    @Override
    public void describeApplication(String project, String application) throws IOException {
        ExecHelper.executeWithTerminal(envVars, command, "app", "describe", application, "--project", project);
    }

    @Override
    public void deleteApplication(String project, String application) throws IOException {
        execute(command, envVars, "app", "delete", application, "-f", "--project", project);
    }

    @Override
    public void push(String project, String application, String context, String component) throws IOException {
        ExecHelper.executeWithTerminal(new File(context), envVars, command, "push");
    }

    @Override
    public void describeComponent(String project, String application, String context, String component) throws IOException {
        if (context != null) {
            ExecHelper.executeWithTerminal(new File(context), envVars, command, "describe");
        } else {
            ExecHelper.executeWithTerminal(envVars, command, "describe", "--project", project, "--app", application, component);
        }
    }

    @Override
    public void watch(String project, String application, String context, String component) throws IOException {
        ExecHelper.executeWithTerminal(new File(context), envVars, command, "watch");
    }

    @Override
    public void createComponentLocal(String project, String application, String componentType, String componentVersion, String component, String source, String devfile, boolean push) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(command);
        args.add("create");
        if (StringUtils.isNotBlank(devfile)) {
            args.add("--devfile");
            args.add(devfile);
        }   else if (StringUtils.isNotBlank(componentVersion)) {
            args.add(componentType + ":" + componentVersion);
        } else {
            args.add(componentType);
        }
        args.add(component);
        args.add("--project");
        args.add(project);
        args.add("--app");
        args.add(application);
        if (StringUtils.isNotBlank(componentVersion)) {
            args.add("--s2i");
        }
        if (push) {
            args.add("--now");
        }
        ExecHelper.executeWithTerminal(new File(source), envVars, args.toArray(new String[0]));
    }

    @Override
    public void createComponentGit(String project, String application, String context, String componentType, String componentVersion, String component, String source, String reference, boolean push) throws IOException {
        if (StringUtils.isNotBlank(reference)) {
            if (push) {
                ExecHelper.executeWithTerminal(new File(context), envVars, command, "create", componentType + ':' + componentVersion, component,
                        "--git", source, "--ref", reference, "--project", project, "--app", application, "--now");
            } else {
                ExecHelper.executeWithTerminal(new File(context), envVars, command, "create", componentType + ':' + componentVersion, component,
                        "--git", source, "--ref", reference, "--project", project, "--app", application);
            }
        } else {
            if (push) {
                ExecHelper.executeWithTerminal(new File(context), envVars, command, "create", componentType + ':' + componentVersion, component,
                        "--git", source, "--project", project, "--app", application, "--now");
            } else {
                ExecHelper.executeWithTerminal(new File(context), envVars, command, "create", componentType + ':' + componentVersion, component,
                        "--git", source, "--project", project, "--app", application);
            }
        }
    }

    @Override
    public void createComponentBinary(String project, String application, String context, String componentType, String componentVersion, String component, String source, boolean push) throws IOException {
        if (push) {
            ExecHelper.executeWithTerminal(new File(context), envVars, command, "create", componentType + ':' + componentVersion, component,
                    "--binary", source, "--project", project, "--app", application, "--now");
        } else {
            ExecHelper.executeWithTerminal(new File(context), envVars, command, "create", componentType + ':' + componentVersion, component,
                    "--binary", source, "--project", project, "--app", application);
        }
    }

    /**
     * ensure that $HOME/.odo/config.yaml file exists so thar we can use service related commands.
     */
    private void ensureDefaultOdoConfigFileExists() {
        File dir = new File(HOME_FOLDER, PLUGIN_FOLDER);
        File config = new File(dir, "config.yaml");
        try {
            if (!config.exists()) {
                dir.mkdirs();
                config.createNewFile();
            }
        } catch (IOException e) {
        }
    }

    @Override
    public void createService(String project, String application, String serviceTemplate, String servicePlan, String service, boolean wait) throws IOException {
        ensureDefaultOdoConfigFileExists();
        if (wait) {
            ExecHelper.executeWithTerminal(new File(HOME_FOLDER), envVars, command, "service", "create", serviceTemplate,
                    "--plan", servicePlan, service, "--app", application, "--project", project, "-w");
        } else {
            ExecHelper.executeWithTerminal(new File(HOME_FOLDER), envVars, command, "service", "create", serviceTemplate,
                    "--plan", servicePlan, service, "--app", application, "--project", project);
        }
    }


    @Override
    public String getServiceTemplate(String project, String application, String service) {
        ServiceCatalogClient sc = client.adapt(ServiceCatalogClient.class);
        return sc.serviceInstances().inNamespace(project).withName(service).get().getMetadata().getLabels().get(NAME_LABEL);
    }

    @Override
    public void deleteService(String project, String application, String service) throws IOException {
        execute(command, envVars, "service", "delete", "--project", project, "--app", application, service, "-f");
    }

    @Override
    public List<ComponentType> getComponentTypes() throws IOException {
        return configureObjectMapper(new ComponentTypesDeserializer()).readValue(
                execute(command, envVars, "catalog", "list", "components", "-o", "json"),
                new TypeReference<List<ComponentType>>() {
                });
    }

    @Override
    public List<ServiceTemplate> getServiceTemplates() throws IOException {
        return configureObjectMapper(new ServiceTemplatesDeserializer()).readValue(
                execute(command, envVars, "catalog", "list", "services", "-o", "json"),
                new TypeReference<List<ServiceTemplate>>() {
                });
    }

    @Override
    public void describeServiceTemplate(String template) throws IOException {
        ensureDefaultOdoConfigFileExists();
        ExecHelper.executeWithTerminal(envVars, command, "catalog", "describe", "service", template);
    }

    @Override
    public List<Integer> getServicePorts(String project, String application, String component) {
        Service service = client.services().inNamespace(project).withName(component + '-' + application).get();
        return service != null ? service.getSpec().getPorts().stream().map(ServicePort::getPort).collect(Collectors.toList()) : new ArrayList<>();
    }

    private static List<URL> parseURLs(String json) throws IOException {
        JSonParser parser = new JSonParser(JSON_MAPPER.readTree(json));
        return parser.parseURLS();
    }

    @Override
    public List<URL> listURLs(String project, String application, String context, String component) throws IOException {
        if (context != null) {
            return parseURLs(execute(new File(context), command, envVars, "url", "list", "-o", "json"));
        } else {
            return parseURLs(execute(command, envVars, "describe", "--project", project, "--app", application, component, "-o", "json"));
        }
    }

    @Override
    public ComponentInfo getComponentInfo(String project, String application, String component, ComponentKind kind) throws IOException {
        if (kind.equals(ComponentKind.S2I)) {
            List<DeploymentConfig> DCs = client.deploymentConfigs().inNamespace(project).withLabel(COMPONENT_NAME_LABEL, component).withLabel(APP_LABEL, application).list().getItems();
            if (DCs.size() == 1) {
                DeploymentConfig deploymentConfig = DCs.get(0);
                ComponentSourceType sourceType = ComponentSourceType.fromAnnotation(deploymentConfig.getMetadata().getAnnotations().get(COMPONENT_SOURCE_TYPE_ANNOTATION));
                ComponentInfo.Builder builder = new ComponentInfo.Builder().withSourceType(sourceType).withComponentTypeName(deploymentConfig.getMetadata().getLabels().get(RUNTIME_NAME_LABEL)).withComponentTypeVersion(deploymentConfig.getMetadata().getLabels().get(RUNTIME_VERSION_LABEL)).withMigrated(deploymentConfig.getMetadata().getLabels().containsKey(ODO_MIGRATED_LABEL));
                if (sourceType == ComponentSourceType.LOCAL) {
                    return builder.build();
                } else if (sourceType == ComponentSourceType.BINARY) {
                    return builder.withBinaryURL(deploymentConfig.getMetadata().getAnnotations().get(VCS_URI_ANNOTATION)).build();
                } else {
                    BuildConfig buildConfig = client.buildConfigs().inNamespace(project).withName(deploymentConfig.getMetadata().getName()).get();
                    return builder.withRepositoryURL(deploymentConfig.getMetadata().getAnnotations().get(VCS_URI_ANNOTATION)).withRepositoryReference(buildConfig.getSpec().getSource().getGit().getRef()).build();
                }
            } else {
                throw new IOException("Invalid number of deployment configs (" + DCs.size() + "), should be 1");
            }
        }
        return parseComponentInfo(execute(command, envVars, "describe", "--project", project, "--app", application, component, "-o", "json"), kind);
    }

    private ComponentInfo parseComponentInfo(String json, ComponentKind kind) throws IOException {
        JSonParser parser = new JSonParser(JSON_MAPPER.readTree(json));
        return parser.parseComponentInfo(kind);
    }

    @Override
    public void createURL(String project, String application, String context, String component, String name, Integer port,
                          boolean secure) throws IOException {
        List<String> args = new ArrayList<>();
        args.add(command);
        args.add("url");
        args.add("create");
        if (StringUtils.isNotEmpty(name)) {
            args.add(name);
        }
        args.add("--port");
        args.add(port.toString());
        if (secure) {
            args.add("--secure");
        }
        ExecHelper.executeWithTerminal(new File(context), envVars, args.toArray(new String[0]));
    }

    @Override
    public void deleteURL(String project, String application, String context, String component, String name) throws IOException {
        execute(new File(context), command, envVars, "url", "delete", "-f", name);
    }

    private void undeployComponent(String project, String application, String context, String component, boolean deleteConfig, ComponentKind kind) throws IOException {
        List<String> args = new ArrayList<>();
        if (context != null) {
            args.add("delete");
            args.add("-f");
            if (deleteConfig) {
                args.add("-a");
            }
            if (ComponentKind.S2I.equals(kind)) {
                args.add("--s2i");
            }
            execute(new File(context), command, envVars, args.toArray(new String[0]));
        } else {
            args.add("delete");
            args.add("-f");
            args.add("--project");
            args.add(project);
            args.add("--app");
            args.add(application);
            args.add(component);
            if (deleteConfig) {
                args.add("-a");
            }
            if (ComponentKind.S2I.equals(kind)) {
                args.add("--s2i");
            }
            execute(command, envVars, args.toArray(new String[0]));
        }
    }

    @Override
    public void undeployComponent(String project, String application, String context, String component, ComponentKind kind) throws IOException {
        undeployComponent(project, application, context, component, false, kind);
    }

    @Override
    public void deleteComponent(String project, String application, String context, String component, ComponentKind kind) throws IOException {
        undeployComponent(project, application, context, component, true, kind);
    }

    @Override
    public void follow(String project, String application, String context, String component) throws IOException {
        ExecHelper.executeWithTerminal(new File(context), envVars, command, "log", "-f");
    }

    @Override
    public void log(String project, String application, String context, String component) throws IOException {
        ExecHelper.executeWithTerminal(new File(context), envVars, command, "log");
    }

    @Override
    public void createProject(String project) throws IOException {
        execute(command, envVars, "project", "create", project);
    }

    @Override
    public void deleteProject(String project) throws IOException {
        execute(command, envVars, "project", "delete", project, "-f");
    }

    @Override
    public void login(String url, String userName, char[] password, String token) throws IOException {
        if (token == null || token.isEmpty()) {
            execute(command, envVars, "login", url, "-u", userName, "-p", String.valueOf(password), " --insecure-skip-tls-verify");
        } else {
            execute(command, envVars, "login", url, "-t", token, " --insecure-skip-tls-verify");
        }
    }

    @Override
    public void logout() throws IOException {
        execute(command, envVars, "logout");
    }

    private static List<Application> parseApplications(String json) throws IOException {
        JSonParser parser = new JSonParser(JSON_MAPPER.readTree(json));
        return parser.parseApplications();
    }

    @Override
    public List<Application> getApplications(String project) throws IOException {
        return parseApplications(execute(command, envVars, "app", "list", "--project", project, "-o", "json")); //TODO issue with odo 2.0.0 and 2.0.1 to correctly parse applications from project.
    }

    @Override
    public List<Component> getComponents(String project, String application) throws IOException {
        return configureObjectMapper(new ComponentDeserializer()).readValue(
                execute(command, envVars, "list", "--app", application, "--project", project, "-o", "json"),
                new TypeReference<List<Component>>() {
                });
    }

    @Override
    public List<ServiceInstance> getServices(String project, String application) {
        ServiceCatalogClient sc = client.adapt(ServiceCatalogClient.class);
        return sc.serviceInstances().inNamespace(project).withLabelSelector(new LabelSelectorBuilder().addToMatchLabels(KubernetesLabels.APP_LABEL, application).build()).list().getItems();
    }

    protected LabelSelector getLabelSelector(String application, String component) {
        return new LabelSelectorBuilder().addToMatchLabels(KubernetesLabels.APP_LABEL, application)
                .addToMatchLabels(KubernetesLabels.COMPONENT_NAME_LABEL, component)
                .build();
    }

    @Override
    public List<Storage> getStorages(String project, String application, String context, String component) throws IOException {
        if (context != null) {
            return configureObjectMapper(new StoragesDeserializer()).readValue(
                    execute(new File(context), command, envVars, "storage", "list", "-o", "json"),
                    new TypeReference<List<Storage>>() {
                    });
        } else {
            return client.persistentVolumeClaims().inNamespace(project).withLabelSelector(getLabelSelector(application, component)).list().getItems()
                    .stream().filter(pvc -> pvc.getMetadata().getLabels().containsKey(KubernetesLabels.STORAGE_NAME_LABEL)).
                            map(pvc -> Storage.of(Storage.getStorageName(pvc))).collect(Collectors.toList());
        }
    }

    @Override
    public void listComponents() throws IOException {
        ExecHelper.executeWithTerminal(envVars, command, "catalog", "list", "components");
    }

    @Override
    public void listServices() throws IOException {
        ExecHelper.executeWithTerminal(envVars, command, "catalog", "list", "services");
    }

    @Override
    public void about() throws IOException {
        ExecHelper.executeWithTerminal(envVars, command, "version");
    }

    @Override
    public void createStorage(String project, String application, String context, String component, String name, String mountPath, String storageSize) throws IOException {
        execute(new File(context), command, envVars, "storage", "create", name, "--path", mountPath, "--size", storageSize);
    }

    @Override
    public void deleteStorage(String project, String application, String context, String component, String storage) throws IOException {
        execute(new File(context), command, envVars, "storage", "delete", storage, "-f");
    }

    @Override
    public void link(String project, String application, String component, String context, String source, Integer port) throws IOException {
        if (port != null) {
            execute(new File(context), command, envVars, "link", source, "--port", port.toString(), "--wait");
        } else {
            execute(new File(context), command, envVars, "link", source, "--wait");
        }
    }

    @Override
    public void debug(String project, String application, String context, String component, Integer port) throws IOException {
        ExecHelper.executeWithTerminal(new File(context), false, envVars, command, "debug", "port-forward",
                "--local-port", port.toString());
    }

    @Override
    public DebugStatus debugStatus(String project, String application, String context, String component) throws IOException {
        String output = ExecHelper.execute(command, new File(context), envVars, "debug", "info");
        if (output.startsWith("Debug is not running"))
            return DebugStatus.NOT_RUNNING;
        else if (output.startsWith("Debug is running"))
            return DebugStatus.RUNNING;
        return DebugStatus.UNKNOWN;
    }


    @Override
    public boolean isServiceCatalogAvailable() {
        return client.isAdaptable(ServiceCatalogClient.class);
    }

    @Override
    public java.net.URL getMasterUrl() {
        return client.getMasterUrl();
    }

    @Override
    public List<Project> getPreOdo10Projects() {
        return getProjects().stream().filter(this::isLegacyProject).collect(Collectors.toList());
    }

    private boolean isLegacyProject(Project project) {
        boolean hasLegacyResources = !client.deploymentConfigs().inNamespace(project.getMetadata().getName()).withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems().isEmpty();
        if (!hasLegacyResources) {
            try {
                hasLegacyResources = !client.adapt(ServiceCatalogClient.class).serviceInstances().inNamespace(project.getMetadata().getName()).withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems().isEmpty();
            } catch (Exception e) {
            }
        }
        return hasLegacyResources;
    }

    @Override
    public List<Exception> migrateProjects(List<Project> projects, BiConsumer<String, String> reporter) {
        List<Exception> exceptions = new ArrayList<>();
        for (Project project : projects) {
            reporter.accept(project.getMetadata().getName(), "deployment configs");
            migrateDCs(client.deploymentConfigs().inNamespace(project.getMetadata().getName()), exceptions);
            reporter.accept(project.getMetadata().getName(), "routes");
            migrateRoutes(client.routes().inNamespace(project.getMetadata().getName()), exceptions);
            reporter.accept(project.getMetadata().getName(), "build configs");
            migrateBuildConfigs(client.buildConfigs().inNamespace(project.getMetadata().getName()), exceptions);
            reporter.accept(project.getMetadata().getName(), "image streams");
            migrateImageStreams(client.imageStreams().inNamespace(project.getMetadata().getName()), exceptions);
            reporter.accept(project.getMetadata().getName(), "services");
            migrateServices(client.services().inNamespace(project.getMetadata().getName()), exceptions);
            reporter.accept(project.getMetadata().getName(), "storages");
            migratePVCs(client.persistentVolumeClaims().inNamespace(project.getMetadata().getName()), exceptions);
            reporter.accept(project.getMetadata().getName(), "secrets");
            migrateSecrets(client.secrets().inNamespace(project.getMetadata().getName()), exceptions);
            reporter.accept(project.getMetadata().getName(), "service instances");
            migrateServiceInstances(client.adapt(ServiceCatalogClient.class).serviceInstances().inNamespace(project.getMetadata().getName()), exceptions);
        }
        return exceptions;
    }

    private void editLabels(Map<String, String> labels) {
        String name = labels.get(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10);
        if (name != null) {
            labels.put(KubernetesLabels.COMPONENT_NAME_LABEL, name);
        }
        name = labels.get(NAME_LABEL);
        if (name != null) {
            labels.put(KubernetesLabels.APP_LABEL, name);
        }
        name = labels.get(KubernetesLabels.COMPONENT_TYPE_LABEL);
        if (name != null) {
            labels.put(KubernetesLabels.NAME_LABEL, name);
        }
        name = labels.get(KubernetesLabels.COMPONENT_VERSION_LABEL);
        if (name != null) {
            labels.put(KubernetesLabels.RUNTIME_VERSION_LABEL, name);
        }
        name = labels.get(KubernetesLabels.URL_NAME_LABEL);
        if (name != null) {
            labels.put(KubernetesLabels.ODO_URL_NAME, name);
        }
        labels.put(ODO_MIGRATED_LABEL, "true");
        labels.remove(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10);
    }

    private void migrateDCs(NonNamespaceOperation<DeploymentConfig, DeploymentConfigList, DoneableDeploymentConfig, DeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig>> operation, List<Exception> exceptions) {
        try {
            for (HasMetadata dc : operation.withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems()) {
                try {
                    DeploymentConfigFluent.MetadataNested<DoneableDeploymentConfig> edit = operation.withName(dc.getMetadata().getName()).edit().editMetadata();
                    editLabels(edit.getLabels());
                    edit.endMetadata().done();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        } catch (Exception e) {
            exceptions.add(e);
        }
    }


    private void migrateRoutes(NonNamespaceOperation<Route, RouteList, DoneableRoute, Resource<Route, DoneableRoute>> operation, List<Exception> exceptions) {
        try {
            for (HasMetadata dc : operation.withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems()) {
                try {
                    RouteFluent.MetadataNested<DoneableRoute> edit = operation.withName(dc.getMetadata().getName()).edit().editMetadata();
                    editLabels(edit.getLabels());
                    edit.endMetadata().done();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        } catch (Exception e) {
            exceptions.add(e);
        }
    }

    private void migrateBuildConfigs(NonNamespaceOperation<BuildConfig, BuildConfigList, DoneableBuildConfig, BuildConfigResource<BuildConfig, DoneableBuildConfig, Void, Build>> operation, List<Exception> exceptions) {
        try {
            for (HasMetadata dc : operation.withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems()) {
                try {
                    BuildConfigFluent.MetadataNested<DoneableBuildConfig> edit = operation.withName(dc.getMetadata().getName()).edit().editMetadata();
                    editLabels(edit.getLabels());
                    edit.endMetadata().done();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        } catch (Exception e) {
            exceptions.add(e);
        }
    }

    private void migrateImageStreams(NonNamespaceOperation<ImageStream, ImageStreamList, DoneableImageStream, Resource<ImageStream, DoneableImageStream>> operation, List<Exception> exceptions) {
        try {
            for (HasMetadata dc : operation.withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems()) {
                try {
                    ImageStreamFluent.MetadataNested<DoneableImageStream> edit = operation.withName(dc.getMetadata().getName()).edit().editMetadata();
                    editLabels(edit.getLabels());
                    edit.endMetadata().done();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        } catch (Exception e) {
            exceptions.add(e);
        }
    }

    private void migrateServices(NonNamespaceOperation<Service, ServiceList, DoneableService, ServiceResource<Service, DoneableService>> operation, List<Exception> exceptions) {
        try {
            for (HasMetadata dc : operation.withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems()) {
                try {
                    ServiceFluent.MetadataNested<DoneableService> edit = operation.withName(dc.getMetadata().getName()).edit().editMetadata();
                    editLabels(edit.getLabels());
                    edit.endMetadata().done();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        } catch (Exception e) {
            exceptions.add(e);
        }
    }

    private void migratePVCs(NonNamespaceOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> operation, List<Exception> exceptions) {
        try {
            for (HasMetadata dc : operation.withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems()) {
                try {
                    PersistentVolumeClaimFluent.MetadataNested<DoneablePersistentVolumeClaim> edit = operation.withName(dc.getMetadata().getName()).edit().editMetadata();
                    editLabels(edit.getLabels());
                    edit.endMetadata().done();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        } catch (Exception e) {
            exceptions.add(e);
        }
    }

    private void migrateSecrets(NonNamespaceOperation<Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>> operation, List<Exception> exceptions) {
        try {
            for (HasMetadata dc : operation.withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems()) {
                try {
                    SecretFluent.MetadataNested<DoneableSecret> edit = operation.withName(dc.getMetadata().getName()).edit().editMetadata();
                    editLabels(edit.getLabels());
                    edit.endMetadata().done();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        } catch (Exception e) {
            exceptions.add(e);
        }
    }

    private void migrateServiceInstances(NonNamespaceOperation<ServiceInstance, ServiceInstanceList, DoneableServiceInstance, ServiceInstanceResource> operation, List<Exception> exceptions) {
        try {
            for (HasMetadata dc : operation.withLabel(KubernetesLabels.COMPONENT_NAME_LABEL_PRE10).list().getItems()) {
                try {
                    ServiceInstanceFluent.MetadataNested<DoneableServiceInstance> edit = operation.withName(dc.getMetadata().getName()).edit().editMetadata();
                    editLabels(edit.getLabels());
                    edit.endMetadata().done();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            }
        } catch (Exception e) {
            //TODO: exception is skipped because of non catalog aware cluster, need to find a way to better deal with that
        }
    }

    @Override
    public String consoleURL() throws IOException {
        try {
            VersionInfo version = client.getVersion();
            if (version == null || "4".equals(version.getMajor())) { // assuming null version is version 4
                ConfigMap configMap = client.configMaps().inNamespace(OCP4_CONFIG_NAMESPACE).withName(OCP4_CONSOLE_PUBLIC_CONFIG_MAP_NAME).get();
                if (configMap != null) {
                    return configMap.getData().get(OCP4_CONSOLE_URL_KEY_NAME);
                }
            } else if ("3".equals(version.getMajor())) {
                ConfigMap configMap = client.configMaps().inNamespace(OCP3_CONFIG_NAMESPACE).withName(OCP3_WEBCONSOLE_CONFIG_MAP_NAME).get();
                String yaml = configMap.getData().get(OCP3_WEBCONSOLE_YAML_FILE_NAME);
                return JSON_MAPPER.readTree(yaml).path("clusterInfo").path("consolePublicURL").asText();
            }
            return client.getMasterUrl() + "console";
        } catch (KubernetesClientException e) {
            return client.getMasterUrl().toExternalForm();
        }
    }

    @Override
    public List<ComponentDescriptor> discover(String path) throws IOException {
        return configureObjectMapper(new ComponentDescriptorsDeserializer()).readValue(
                execute(new File(path), command, envVars, "list", "--path", ".", "-o", "json"),
                new TypeReference<List<ComponentDescriptor>>() {
                });
    }

    @Override
    public ComponentKind getComponentKind(String context) throws IOException {
        String json = execute(new File(context), command, envVars, "list", "--path", ".", "-o", "json");
        JsonNode root = JSON_MAPPER.readTree(json);
        if (root.get("s2iComponents").size() != 0) {
            return ComponentKind.S2I;
        }
        if (root.get("devfileComponents").size() != 0) {
            return ComponentKind.DEVFILE;
        }
        return null;
    }
}
