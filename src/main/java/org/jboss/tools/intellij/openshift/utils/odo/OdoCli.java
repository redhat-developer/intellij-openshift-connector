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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.redhat.devtools.intellij.common.kubernetes.ClusterHelper;
import com.redhat.devtools.intellij.common.kubernetes.ClusterInfo;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.NetworkUtils;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.http.HttpRequest;
import io.fabric8.kubernetes.client.http.HttpResponse;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.OpenShiftClient;
import org.apache.commons.lang3.StringUtils;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.IS_OPENSHIFT;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.KUBERNETES_VERSION;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.OPENSHIFT_VERSION;

public class OdoCli implements Odo {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdoCli.class);

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final String WINDOW_TITLE = "OpenShift";

    private static final String METADATA_FIELD = "metadata";
    private static final String NAME_FIELD = "name";
    private static final String NAMESPACE_FIELD = "namespace";
    private static final String SPEC_FIELD = "spec";

    private final com.intellij.openapi.project.Project project;
    private final String command;

    private final KubernetesClient client;

    private Map<String, String> envVars;

    private String namespace;

    private final AtomicBoolean swaggerLoaded = new AtomicBoolean();

    private JSonParser swagger;

    private Map<String, Map<ComponentFeature, ProcessHandler>> componentProcesses = new HashMap<>();

    OdoCli(com.intellij.openapi.project.Project project, String command) {
        this.command = command;
        this.project = project;
        this.client = new DefaultKubernetesClient(new ConfigBuilder().build());
        try {
            this.envVars = NetworkUtils.buildEnvironmentVariables(this.getMasterUrl().toString());
            this.envVars.put("ODO_DISABLE_TELEMETRY", "true");
        } catch (URISyntaxException e) {
            this.envVars = Collections.emptyMap();
        }
        reportTelemetry();
    }

    private void reportTelemetry() {
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().getBuilder().action(TelemetryService.NAME_PREFIX_MISC + "login");
        try {
            ClusterInfo info = ClusterHelper.getClusterInfo(client);
            telemetry.property(KUBERNETES_VERSION, info.getKubernetesVersion());
            telemetry.property(IS_OPENSHIFT, Boolean.toString(info.isOpenshift()));
            telemetry.property(OPENSHIFT_VERSION, info.getOpenshiftVersion());
            telemetry.send();
        } catch (RuntimeException e) {
            // do not send telemetry when there is no context ( ie default kube URL as master URL )
            try {
                //workaround to not send null values
                if (e.getMessage() != null) {
                    telemetry.error(e).send();
                } else {
                    telemetry.error(e.toString()).send();
                }
            } catch (RuntimeException ex) {
                LOGGER.warn(ex.getLocalizedMessage(), ex);
            }
        }
    }


    private ObjectMapper configureObjectMapper(final StdNodeBasedDeserializer<? extends List<?>> deserializer) {
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(List.class, deserializer);
        return JSON_MAPPER.registerModule(module);
    }

    @Override
    public List<String> getNamespaces() throws IOException {
        try {
            if (isOpenShift()) {
                return client.adapt(OpenShiftClient.class).projects().list().getItems().stream().
                        map(p -> p.getMetadata().getName()).collect(Collectors.toList());
            } else {
                return client.namespaces().list().getItems().stream().
                        map(p -> p.getMetadata().getName()).collect(Collectors.toList());
            }
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    protected String validateNamespace(String ns) {
        if (Strings.isNullOrEmpty(ns)) {
            ns = "default";
        }
        try {
            if (isOpenShift()) {
                client.adapt(OpenShiftClient.class).projects().withName(ns).get();
            } else {
                client.namespaces().withName(ns).get();
            }
        } catch (KubernetesClientException e) {
            ns = "";
            if (isOpenShift()) {
                List<Project> projects = client.adapt(OpenShiftClient.class).projects().list().getItems();
                if (!projects.isEmpty()) {
                    ns = projects.get(0).getMetadata().getNamespace();
                }
            } else {
                List<Namespace> namespaces = client.namespaces().list().getItems();
                if (!namespaces.isEmpty()) {
                    ns = namespaces.get(0).getMetadata().getNamespace();
                }
            }
        }
        return ns;
    }

    @Override
    public String getNamespace() {
        if (namespace == null) {
            namespace = validateNamespace(client.getNamespace());
        }
        return "".equals(namespace)?null:namespace;
    }

    private static String execute(File workingDirectory, String command, Map<String, String> envs, String... args) throws IOException {
        ExecHelper.ExecResult output = ExecHelper.executeWithResult(command, true, workingDirectory, envs, args);
        try (BufferedReader reader = new BufferedReader(new StringReader(output.getStdOut()))) {
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
    public void start(String project, String context, String component, ComponentFeature feature) throws IOException {
        Map<ComponentFeature, ProcessHandler> componentMap = componentProcesses.computeIfAbsent(component, name -> new HashMap<>());
        ProcessHandler handler = componentMap.get(feature);
        if (handler == null) {
            List<String> args = new ArrayList<>();
            args.add(command);
            args.addAll(feature.getArgs());
            ExecHelper.executeWithTerminal(
                    this.project, WINDOW_TITLE,
                    new File(context),
                    false,
                    envVars,
                    (ConsoleView) null,
                    null,
                    new ProcessAdapter() {
                        @Override
                        public void startNotified(@NotNull ProcessEvent event) {
                            componentMap.put(feature, event.getProcessHandler());
                        }

                        @Override
                        public void processTerminated(@NotNull ProcessEvent event) {
                            componentMap.remove(feature);
                        }
                    },
                    args.toArray(new String[args.size()]));
        }
    }

    private void stopHandler(ProcessHandler handler) {
        handler.destroyProcess();
    }

    @Override
    public void stop(String project, String context, String component, ComponentFeature feature) throws IOException {
        Map<ComponentFeature, ProcessHandler> componentMap = componentProcesses.computeIfAbsent(component, name -> new HashMap<>());
        ProcessHandler handler = componentMap.remove(feature);
        if (handler != null) {
            stopHandler(handler);
        }
    }

    @Override
    public boolean isStarted(String project, String context, String component, ComponentFeature feature) throws IOException {
        Map<ComponentFeature, ProcessHandler> componentMap = componentProcesses.computeIfAbsent(component, name -> new HashMap<>());
        return componentMap.containsKey(feature);
    }

    @Override
    public void describeComponent(String project, String context, String component) throws IOException {
        ExecHelper.executeWithTerminal(this.project, WINDOW_TITLE, createWorkingDirectory(context), false, envVars, command, "describe");
    }

    @Override
    public List<ComponentMetadata> analyze(String path) throws IOException {
        return configureObjectMapper(new ComponentMetadatasDeserializer()).readValue(
                execute(new File(path), command, envVars, "analyze", "-o", "json"),
                new TypeReference<List<ComponentMetadata>>() {
                });
    }

    @Override
    public void createComponent(String project, String componentType, String registryName, String component, String source, String devfile, String starter) throws IOException {
        List<String> args = new ArrayList<>();
        args.add("init");
        if (StringUtils.isNotBlank(devfile)) {
            args.add("--devfile-path");
            args.add(devfile);
        } else {
            if (StringUtils.isNotBlank(starter)) {
                args.add("--starter");
                args.add(starter);
            }
            args.add("--devfile");
            args.add(componentType);
            args.add("--devfile-registry");
            args.add(registryName);
        }
        args.add("--name");
        args.add(component);
        execute(new File(source), command, envVars, args.toArray(new String[0]));
    }

    /**
     * ensure that $HOME/.odo/config.yaml file exists so thar we can use service related commands.
     */
    private void ensureDefaultOdoConfigFileExists() {
        Path dir = Paths.get(HOME_FOLDER, PLUGIN_FOLDER);
        Path config = dir.resolve("config.yaml");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            if (!Files.exists(config)) {
                Files.createFile(config);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private CustomResourceDefinitionContext toCustomResourceDefinitionContext(OperatorCRD crd) {
        String group = crd.getName().substring(crd.getName().indexOf('.') + 1);
        String plural = crd.getName().substring(0, crd.getName().indexOf('.'));
        return new CustomResourceDefinitionContext.Builder()
                .withName(crd.getName())
                .withGroup(group)
                .withScope("Namespaced")
                .withKind(crd.getKind())
                .withPlural(plural)
                .withVersion(crd.getVersion())
                .build();
    }

    private CustomResourceDefinitionContext toCustomResourceDefinitionContext(org.jboss.tools.intellij.openshift.utils.odo.Service service) {
        String version = service.getApiVersion().substring(service.getApiVersion().indexOf('/') + 1);
        String group = service.getApiVersion().substring(0, service.getApiVersion().indexOf('/'));
        return new CustomResourceDefinitionContext.Builder()
                .withName(service.getKind().toLowerCase() + "s." + group)
                .withGroup(group)
                .withScope("Namespaced")
                .withKind(service.getKind())
                .withPlural(service.getKind().toLowerCase() + "s")
                .withVersion(version)
                .build();
    }

    @Override
    public void createService(String project, ServiceTemplate serviceTemplate, OperatorCRD serviceCRD,
                              String service, ObjectNode spec, boolean wait) throws IOException {
        try {
            CustomResourceDefinitionContext context = toCustomResourceDefinitionContext(serviceCRD);
            ObjectNode payload = serviceCRD.getSample().deepCopy();
            updatePayload(payload, spec, project, service);
            client.customResource(context).create(project, JSON_MAPPER.writeValueAsString(payload));
        } catch (KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    private void updatePayload(JsonNode node, JsonNode spec, String project, String service) {
        ((ObjectNode)node.get(METADATA_FIELD)).set(NAME_FIELD, JSON_MAPPER.getNodeFactory().textNode(service));
        ((ObjectNode)node.get(METADATA_FIELD)).set(NAMESPACE_FIELD, JSON_MAPPER.getNodeFactory().textNode(project));
        if (spec != null) {
            ((ObjectNode)node).set(SPEC_FIELD, spec);
        }
    }

    @Override
    public String getServiceTemplate(String project, String service) throws IOException {
        throw new IOException("Not implemented by odo yet");
    }

    @Override
    public void deleteService(String project, org.jboss.tools.intellij.openshift.utils.odo.Service service) throws IOException {
        try {
            CustomResourceDefinitionContext context = toCustomResourceDefinitionContext(service);
            client.customResource(context).delete(project, service.getName());
        } catch (KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public List<DevfileComponentType> getComponentTypes() throws IOException {
        return configureObjectMapper(new ComponentTypesDeserializer()).readValue(
                execute(command, envVars, "registry", "list", "-o", "json"),
                new TypeReference<List<DevfileComponentType>>() {
                });
    }


    private void loadSwagger() {
        try {

            HttpRequest req = client.getHttpClient().newHttpRequestBuilder().url(new java.net.URL(client.getMasterUrl(), "/openapi/v2")).build();
            HttpResponse<String> response = client.getHttpClient().send(req, String.class);
            if (response.isSuccessful()) {
                swagger = new JSonParser(new ObjectMapper().readTree(response.body()));
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    private ObjectNode findSchema(String crd) {
        try {
            if (swaggerLoaded.compareAndSet(false, true)) {
                loadSwagger();
            }
            if (swagger != null) {
                return swagger.findSchema("/apis/" + crd);
            }
        } catch (IOException e) {}
        return null;
    }

    @Override
    public List<ServiceTemplate> getServiceTemplates() throws IOException {
        return configureObjectMapper(new ServiceTemplatesDeserializer(this::findSchema)).readValue(
                execute(command, envVars, "catalog", "list", "services", "-o", "json"),
                new TypeReference<List<ServiceTemplate>>() {
                });
    }

    @Override
    public void describeServiceTemplate(String template) throws IOException {
        ensureDefaultOdoConfigFileExists();
        ExecHelper.executeWithTerminal(this.project, WINDOW_TITLE, true, envVars, command, "catalog", "describe", "service", template);
    }

    private List<URL> parseURLs(String json) throws IOException {
        JSonParser parser = new JSonParser(JSON_MAPPER.readTree(json));
        return parser.parseURLS();
    }

    @Override
    public List<URL> listURLs(String project, String context, String component) throws IOException {
        if (context != null) {
            return parseURLs(execute(new File(context), command, envVars, "url", "list", "-o", "json"));
        } else {
            return parseURLs(execute(command, envVars, "describe", "--project", project, component, "-o", "json"));
        }
    }

    @Override
    public ComponentInfo getComponentInfo(String project, String component, String path,
                                          ComponentKind kind) throws IOException {
        if (path != null) {
            return parseComponentInfo(execute(new File(path), command, envVars, "describe", "component", "-o", "json"), kind);
        } else {
            return parseComponentInfo(execute(command, envVars, "describe", "component", "--namespace", project, "--name", component, "-o", "json"), kind);
        }
    }

    private ComponentInfo parseComponentInfo(String json, ComponentKind kind) throws IOException {
        JSonParser parser = new JSonParser(JSON_MAPPER.readTree(json));
        return parser.parseDescribeComponentInfo(kind);
    }

    @Override
    public void createURL(String project, String context, String component, String name, Integer port,
                          boolean secure, String host) throws IOException {
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
        if (!isOpenShift()){
            args.add("--host");
            args.add(host);
        }
        ExecHelper.executeWithTerminal(this.project, WINDOW_TITLE, new File(context), true, envVars, args.toArray(new String[0]));
    }

    @Override
    public void deleteURL(String project, String context, String component, String name) throws IOException {
        execute(new File(context), command, envVars, "url", "delete", "-f", name);
    }

    /*
     * We should emulate oc delete all -l app.kubernetes.io/component=comp_name but as the Kubernetes client does not allow
     * to retrieve all APIGroups we reduce the scope to:
     * - Deployment
     * - Service
     * - Route
     * - BuildConfig
     * - ImageStreams
     */
    private void deleteDeployment(String project, String deployment) throws IOException {
        try {
            client.apps().deployments().inNamespace(project).withName(deployment)
                    .withPropagationPolicy(DeletionPropagation.BACKGROUND).delete();
            client.services().inNamespace(project).withLabel(KubernetesLabels.COMPONENT_LABEL, deployment).list()
                    .getItems().forEach(service -> client.services().withName(service.getMetadata().getName())
                            .withPropagationPolicy(DeletionPropagation.BACKGROUND).delete());
            if (client.isAdaptable(OpenShiftClient.class)) {
                OpenShiftClient oclient = client.adapt(OpenShiftClient.class);
                oclient.routes().inNamespace(project).withLabelIn(KubernetesLabels.COMPONENT_LABEL, deployment).list()
                        .getItems().forEach(route -> oclient.routes().withName(route.getMetadata().getName())
                                .withPropagationPolicy(DeletionPropagation.BACKGROUND).delete());
                oclient.buildConfigs().inNamespace(project).withLabel(KubernetesLabels.COMPONENT_LABEL, deployment)
                        .list().getItems().forEach(bc -> oclient.buildConfigs().withName(bc.getMetadata().getName())
                                .withPropagationPolicy(DeletionPropagation.BACKGROUND).delete());
                oclient.imageStreams().inNamespace(project).withLabel(KubernetesLabels.COMPONENT_LABEL, deployment)
                        .list().getItems().forEach(is -> oclient.imageStreams().withName(is.getMetadata().getName())
                                .withPropagationPolicy(DeletionPropagation.BACKGROUND).delete());
            }
        } catch (KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    private void undeployComponent(String project, String context, String component,
                                   boolean deleteConfig, ComponentKind kind) throws IOException {
        if (kind != ComponentKind.OTHER) {
            List<String> args = new ArrayList<>();
            args.add("delete");
            args.add("-f");
            if (context != null) {
                if (deleteConfig) {
                    args.add("-a");
                }
                execute(new File(context), command, envVars, args.toArray(new String[0]));
            } else {
                args.add("--project");
                args.add(project);
                args.add(component);
                execute(command, envVars, args.toArray(new String[0]));
            }
        } else {
            deleteDeployment(project, component);
        }
    }

    @Override
    public void undeployComponent(String project, String context, String component, ComponentKind kind) throws IOException {
        undeployComponent(project,context, component, false, kind);
    }

    @Override
    public void deleteComponent(String project, String context, String component, ComponentKind kind) throws IOException {
        undeployComponent(project, context, component, true, kind);
    }

    @Override
    public void follow(String project, String context, String component) throws IOException {
        ExecHelper.executeWithTerminal(
                this.project, WINDOW_TITLE,
                createWorkingDirectory(context),
                true,
                envVars,
                command,
                "log", "-f");
    }

    @Override
    public void log(String project, String context, String component) throws IOException {
        ExecHelper.executeWithTerminal(
                this.project,
                WINDOW_TITLE,
                createWorkingDirectory(context),
                true,
                envVars,
                command,
                "log");
    }

    @Nullable
    private File createWorkingDirectory(String context) {
        File workingDirectory = null;
        if (context != null) {
            workingDirectory = new File(context);
        }
        return workingDirectory;
    }

    @Override
    public void createProject(String project) throws IOException {
        execute(command, envVars, "project", "create", project, "-w");
    }

    @Override
    public void deleteProject(String project) throws IOException {
        execute(command, envVars, "project", "delete", project, "-f", "-w");
    }

    @Override
    public void login(String url, String userName, char[] password, char[] token) throws IOException {
        if (token == null) {
            execute(command, envVars, "login", url, "-u", userName, "-p", String.valueOf(password), " --insecure-skip-tls-verify");
        } else {
            execute(command, envVars, "login", url, "-t", String.valueOf(token), " --insecure-skip-tls-verify");
        }
    }

    @Override
    public void logout() throws IOException {
        execute(command, envVars, "logout");
    }

    @Override
    public List<Component> getComponents(String project) throws IOException {
        return configureObjectMapper(new ComponentDeserializer()).readValue(
                execute(command, envVars, "list", "--namespace", project, "-o", "json"),
                new TypeReference<List<Component>>() {
                });
    }

    @Override
    public List<org.jboss.tools.intellij.openshift.utils.odo.Service> getServices(String project) throws IOException {
        /*try {
            return configureObjectMapper(new ServiceDeserializer()).readValue(
                    execute(command, envVars, "service", "list", "--project", project, "-o", "json"),
                    new TypeReference<List<org.jboss.tools.intellij.openshift.utils.odo.Service>>() {
                    });
        } catch (IOException e) {
            //https://github.com/openshift/odo/issues/5010
            if (e.getMessage().contains("\"no operator backed services found in namespace:") || (e.getMessage().contains("failed to list Operator backed services"))) {
                return Collections.emptyList();
            }
            throw e;
        }*/
        return Collections.emptyList();
    }

    protected LabelSelector getLabelSelector(String application, String component) {
        return new LabelSelectorBuilder().addToMatchLabels(KubernetesLabels.APP_LABEL, application)
                .addToMatchLabels(KubernetesLabels.COMPONENT_NAME_LABEL, component)
                .build();
    }

    @Override
    public void listComponents() throws IOException {
        ExecHelper.executeWithTerminal(this.project, WINDOW_TITLE, true, envVars, command, "catalog", "list", "components");
    }

    @Override
    public void listServices() throws IOException {
        ExecHelper.executeWithTerminal(this.project, WINDOW_TITLE, true, envVars, command, "catalog", "list", "services");
    }

    @Override
    public void about() throws IOException {
        ExecHelper.executeWithTerminal(this.project, WINDOW_TITLE, true, envVars, command, "version");
    }

    @Override
    public void link(String project, String context, String component, String target) throws IOException {
        execute(new File(context), command, envVars, "link", target);
    }

    @Override
    public void debug(String project, String context, String component, Integer port) throws IOException {
        ExecHelper.executeWithTerminal(
                this.project,
                WINDOW_TITLE,
                createWorkingDirectory(context),
                false,
                envVars,
                command,
                "debug", "port-forward", "--local-port", port.toString());
    }

    @Override
    public DebugStatus debugStatus(String project, String context, String component) throws IOException {
        try {
            String json = execute(new File(context), command, envVars, "debug", "info", "-o", "json");
            JSonParser parser = new JSonParser(JSON_MAPPER.readTree(json));
            return parser.parseDebugStatus();
        } catch (IOException e) {
            if (e.getMessage().contains("debug is not running")) {
                return DebugStatus.NOT_RUNNING;
            }
            throw e;
        }
    }

    @Override
    public java.net.URL getMasterUrl() {
        return client.getMasterUrl();
    }

    @Override
    public String consoleURL() throws IOException {
        try {
            VersionInfo version = client.getVersion();
            if (isOpenShift() && "4".equals(version.getMajor())) { // assuming kubernetes version 1 is version 4
                ConfigMap configMap = client.configMaps().inNamespace(OCP4_CONFIG_NAMESPACE).withName(OCP4_CONSOLE_PUBLIC_CONFIG_MAP_NAME).get();
                if (configMap != null) {
                    return configMap.getData().get(OCP4_CONSOLE_URL_KEY_NAME);
                }
            } else if (isOpenShift() && "3".equals(version.getMajor())) {
                ConfigMap configMap = client.configMaps().inNamespace(OCP3_CONFIG_NAMESPACE).withName(OCP3_WEBCONSOLE_CONFIG_MAP_NAME).get();
                String yaml = configMap.getData().get(OCP3_WEBCONSOLE_YAML_FILE_NAME);
                return JSON_MAPPER.readTree(yaml).path("clusterInfo").path("consolePublicURL").asText();
            }
            //https://<master-ip>:<apiserver-port>/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
            return client.getMasterUrl() + "console";
        } catch (KubernetesClientException e) {
            return client.getMasterUrl().toExternalForm();
        }
    }

    @Override
    public boolean isOpenShift(){
        return ClusterHelper.getClusterInfo(client).isOpenshift();
    }

    @Override
    public List<ComponentDescriptor> discover(String path) throws IOException {
        return configureObjectMapper(new ComponentDescriptorsDeserializer()).readValue(
                execute(new File(path), command, envVars, "list", "-o", "json"),
                new TypeReference<List<ComponentDescriptor>>() {
                });
    }

    @Override
    public ComponentTypeInfo getComponentTypeInfo(String componentType, String registryName) throws IOException {
        String json = execute(command, envVars, "registry", "list", "--devfile-registry", registryName, "--devfile", componentType, "-o", "json");
        JSonParser parser = new JSonParser(JSON_MAPPER.readTree(json));
        return parser.parseComponentTypeInfo();
    }

    @Override
    public List<DevfileRegistry> listDevfileRegistries() throws IOException {
        //see https://github.com/redhat-developer/odo/issues/5993
        Path dir = Paths.get(HOME_FOLDER, PLUGIN_FOLDER);
        Path config = dir.resolve("preference.yaml");
        if (Files.exists(config)) {
            JsonNode root = YAML_MAPPER.readTree(config.toFile());
            JSonParser parser = new JSonParser(root);
            return parser.parseRegistries();
        }
        return Collections.emptyList();
    }

    @Override
    public void createDevfileRegistry(String name, String url, String token) throws IOException {
        if (StringUtils.isNotBlank(token)) {
            execute(command, envVars, "registry", "add", name, url, "--token", token);
        } else {
            execute(command, envVars, "registry", "add", name, url);
        }
    }

    @Override
    public void deleteDevfileRegistry(String name) throws IOException {
        execute(command, envVars, "registry", "delete", "-f", name);
    }

    @Override
    public List<DevfileComponentType> getComponentTypes(String name) throws IOException {
        return getComponentTypes().stream().
                filter(type -> name.equals(type.getDevfileRegistry().getName())).
                collect(Collectors.toList());
    }
}
