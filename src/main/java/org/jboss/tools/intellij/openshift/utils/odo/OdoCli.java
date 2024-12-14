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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.util.messages.MessageBus;
import com.redhat.devtools.intellij.common.kubernetes.ClusterHelper;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import io.fabric8.kubernetes.api.Pluralize;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.http.HttpRequest;
import io.fabric8.kubernetes.client.http.HttpResponse;
import io.fabric8.kubernetes.model.Scope;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.OpenShiftOperatorHubAPIGroupDSL;
import io.fabric8.openshift.client.impl.OpenShiftOperatorHubAPIGroupClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.utils.Cli;
import org.jboss.tools.intellij.openshift.utils.ClientAwareCli;
import org.jboss.tools.intellij.openshift.utils.KubernetesClientExceptionUtils;
import org.jboss.tools.intellij.openshift.utils.Serialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.tools.intellij.openshift.Constants.HOME_FOLDER;
import static org.jboss.tools.intellij.openshift.Constants.OCP3_CONFIG_NAMESPACE;
import static org.jboss.tools.intellij.openshift.Constants.OCP3_WEBCONSOLE_CONFIG_MAP_NAME;
import static org.jboss.tools.intellij.openshift.Constants.OCP3_WEBCONSOLE_YAML_FILE_NAME;
import static org.jboss.tools.intellij.openshift.Constants.OCP4_CONFIG_NAMESPACE;
import static org.jboss.tools.intellij.openshift.Constants.OCP4_CONSOLE_PUBLIC_CONFIG_MAP_NAME;
import static org.jboss.tools.intellij.openshift.Constants.OCP4_CONSOLE_URL_KEY_NAME;
import static org.jboss.tools.intellij.openshift.Constants.PLUGIN_FOLDER;

public class OdoCli extends ClientAwareCli implements OdoDelegate {

  private static final Logger LOGGER = LoggerFactory.getLogger(OdoCli.class);

  private static final String DEFAULT_NAMESPACE = "default";
  private static final String WINDOW_TITLE = "OpenShift";

  private static final String METADATA_FIELD = "metadata";
  private static final String NAME_FIELD = "name";
  private static final String NAMESPACE_FIELD = "namespace";
  private static final String SPEC_FIELD = "spec";
  private final com.intellij.openapi.project.Project project;
  private final OpenShiftClient openshiftClient;
  private final Map<String, String> envVars;
  private final AtomicBoolean swaggerLoaded = new AtomicBoolean();
  private String currentNamespace;
  private JSonParser swagger;
  private CompletableFuture<Boolean> isPodmanPresent;

  public OdoCli(String command, Project project, KubernetesClient client) {
    this(command,
      project,
      client,
      ApplicationManager.getApplication().getMessageBus(),
      new OpenShiftClientFactory(),
      new Cli.EnvVarFactory(),
      new Cli.TelemetryReport());
  }

  protected OdoCli(
    String command,
    Project project,
    KubernetesClient client,
    MessageBus bus,
    Function<KubernetesClient, OpenShiftClient> openshiftClientFactory,
    Function<String, Map<String, String>> envVarFactory,
    Cli.TelemetryReport telemetryReport) {
    super(command, client);
    this.project = project;
    this.openshiftClient = openshiftClientFactory.apply(client);
    this.envVars = envVarFactory.apply(String.valueOf(client.getMasterUrl()));
    this.isPodmanPresent = processPodmanPresent(command);
    initTelemetry(bus, telemetryReport);
  }

  private void initTelemetry(MessageBus bus, TelemetryReport telemetryReport) {
    telemetryReport.addTelemetryVars(envVars);
    telemetryReport.subscribe(bus, envVars);
    telemetryReport.report(client);
  }


  private static String execute(@NotNull File workingDirectory, String command, Map<String, String> envs, String... args) throws IOException {
    ExecHelper.ExecResult output = ExecHelper.executeWithResult(command, true, workingDirectory, envs, args);
    try (BufferedReader reader = new BufferedReader(new StringReader(output.getStdOut()))) {
      BinaryOperator<String> reducer = new BinaryOperator<>() {
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
  public List<String> getNamespaces() throws IOException {
    try {
      return getNamespacesOrProjects().stream()
        .map(resource -> resource.getMetadata().getName()).toList();
    } catch (KubernetesClientException e) {
      throw new IOException(e);
    }
  }

  private List<? extends HasMetadata> getNamespacesOrProjects() {
    if (isOpenShift()) {
      return openshiftClient.projects().list().getItems();
    } else {
      return client.namespaces().list().getItems();
    }
  }

  @Override
  public String getCurrentNamespace() {
    if (currentNamespace == null) {
      currentNamespace = getCurrentNamespace(client.getNamespace());
    }
    return currentNamespace;
  }

  private String getCurrentNamespace(String name) {
    String namespace = name;
    if (Strings.isEmpty(name)) {
      namespace = DEFAULT_NAMESPACE;
    }
    return namespace;
  }

  @Override
  public boolean namespaceExists(String name) {
    try {
      if (isOpenShift()) {
        return openshiftClient.projects().withName(name).get() != null;
      } else {
        return client.namespaces().withName(name).get() != null;
      }
    } catch (KubernetesClientException e) {
      return false;
    }
  }

  @Override
  public String getNamespaceKind() {
    if (isOpenShift()) {
      return "Project";
    } else {
      return "Namespace";
    }
  }

  @Override
  public void start(String context, ComponentFeature feature, ProcessHandler handler, ProcessAdapter processAdapter) throws IOException {
    if (handler == null) {
      List<String> args = new ArrayList<>();
      args.add(command);
      args.addAll(feature.getStartArgs());
      ExecHelper.executeWithTerminal(
        this.project, WINDOW_TITLE,
        new File(context),
        false,
        envVars,
        null,
        null,
        processAdapter,
        args.toArray(new String[0]));
    }
  }

  @Override
  public void stop(String context, ComponentFeature feature, ProcessHandler handler) throws IOException {
    if (context != null && handler != null) {
      handler.destroyProcess();
      if (!feature.getStopArgs().isEmpty()) {
        execute(createWorkingDirectory(context), command, envVars, feature.getStopArgs().toArray(new String[0]));
      }
    }
  }

  @Override
  public void describeComponent(String context) throws IOException {
    if (context != null) {
      ExecHelper.executeWithTerminal(this.project, WINDOW_TITLE, createWorkingDirectory(context), false, envVars, command, "describe", "component");
    }
  }

  @Override
  public List<ComponentMetadata> analyze(String path) throws IOException {
    return Serialization.configure(new ComponentMetadatasDeserializer()).readValue(
      execute(new File(path), command, envVars, "analyze", "-o", "json"),
      new TypeReference<>() {
      });
  }

  @Override
  public void createComponent(String componentType, String registryName, String component, String source, String devfile, String starter) throws IOException {
    List<String> args = new ArrayList<>();
    args.add("init");
    if (!Strings.isEmptyOrSpaces(devfile)) {
      args.add("--devfile-path");
      args.add(devfile);
    } else {
      if (!Strings.isEmptyOrSpaces(starter)) {
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

  private CustomResourceDefinitionContext toCustomResourceDefinitionContext(org.jboss.tools.intellij.openshift.utils.odo.Service service) {
    String version = service.getApiVersion().substring(service.getApiVersion().indexOf('/') + 1);
    String group = service.getApiVersion().substring(0, service.getApiVersion().indexOf('/'));
    return new CustomResourceDefinitionContext.Builder()
      .withName(service.getKind().toLowerCase() + "s." + group)
      .withGroup(group)
      .withScope(Scope.NAMESPACED.value())
      .withKind(service.getKind())
      .withPlural(Pluralize.toPlural(service.getKind().toLowerCase()))
      .withVersion(version)
      .build();
  }

  @Override
  public void createService(String project, ServiceTemplate serviceTemplate, OperatorCRD serviceCRD,
                            String service, ObjectNode spec, boolean wait) throws IOException {
    try {
      ObjectNode payload = serviceCRD.getSample().deepCopy();
      updatePayload(payload, spec, project, service);
      client.resource(Serialization.json().writeValueAsString(payload)).create();
    } catch (KubernetesClientException e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  private void updatePayload(JsonNode node, JsonNode spec, String project, String service) {
    ObjectNode objectNode = (ObjectNode) node;
    ObjectNode metadataField = (ObjectNode) objectNode.get(METADATA_FIELD);
    metadataField.set(NAME_FIELD, Serialization.json().getNodeFactory().textNode(service));
    metadataField.set(NAMESPACE_FIELD, Serialization.json().getNodeFactory().textNode(project));
    if (spec != null) {
      objectNode.set(SPEC_FIELD, spec);
    }
  }

  @Override
  public void deleteService(String project, org.jboss.tools.intellij.openshift.utils.odo.Service service) throws IOException {
    try {
      CustomResourceDefinitionContext context = toCustomResourceDefinitionContext(service);
      client.genericKubernetesResources(context).inNamespace(project).withName(service.getName()).delete();
    } catch (KubernetesClientException e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public List<DevfileComponentType> getAllComponentTypes() throws IOException {
    return Serialization.configure(new ComponentTypesDeserializer()).readValue(
      execute(command, envVars, "registry", "list", "-o", "json"),
      new TypeReference<>() {
      });
  }

  private void loadSwagger() {
    try {
      HttpRequest req = client.getHttpClient().newHttpRequestBuilder().url(new java.net.URL(client.getMasterUrl(), "/openapi/v2")).build();
      CompletableFuture<HttpResponse<byte[]>> completableFuture = client.getHttpClient()
        .sendAsync(req, byte[].class);
      HttpResponse<byte[]> response = completableFuture.get();
      if (response.isSuccessful()) {
        swagger = new JSonParser(new ObjectMapper().readTree(response.body()));
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException | ExecutionException e) {
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
    } catch (IOException e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
    }
    return null;
  }

  private void getTargetCRD(GenericKubernetesResource resource,
                            List<GenericKubernetesResource> bindableKinds) {
    JsonNode additionalPropertiesNode = client.getKubernetesSerialization().convertValue(resource.getAdditionalProperties(), JsonNode.class);
    if (additionalPropertiesNode != null &&
      additionalPropertiesNode.has("status")) {
      for (JsonNode status : additionalPropertiesNode.get("status")) {
        if (status.has("group") && status.has("kind") && status.has("version")) {
          GenericKubernetesResource bindableKind = new GenericKubernetesResource();
          bindableKind.setApiVersion(status.get("group").asText() + '/' + status.get("version").asText());
          bindableKind.setKind(status.get("kind").asText());
          bindableKinds.add(bindableKind);
        }
      }
    }
  }

  private List<GenericKubernetesResource> getBindableKinds() {
    List<GenericKubernetesResource> bindableKinds = new ArrayList<>();
    client.genericKubernetesResources("binding.operators.coreos.com/v1alpha1", "BindableKinds")
      .list()
      .getItems()
      .forEach(r -> getTargetCRD(r, bindableKinds));
    return bindableKinds;
  }

  @Override
  public List<ServiceTemplate> getServiceTemplates() {
    try {
      List<GenericKubernetesResource> bindableKinds = getBindableKinds();
      // if cluster (either openshift or Kubernetes) supports  operators
      OpenShiftOperatorHubAPIGroupDSL hubClient = client.adapt(OpenShiftOperatorHubAPIGroupClient.class);
      ServiceTemplatesDeserializer deserializer = new ServiceTemplatesDeserializer(this::findSchema, bindableKinds);
      return deserializer.fromList(hubClient.clusterServiceVersions().list());
    } catch (KubernetesClientException e) {
      // if client can't be adapted to OperatorHub
      return Collections.emptyList();
    }
  }

  private List<URL> parseURLs(String json) throws IOException {
    JSonParser parser = new JSonParser(Serialization.json().readTree(json));
    return parser.parseURLS();
  }

  @Override
  public List<URL> listURLs(String context) throws IOException {
    if (context != null) {
      return parseURLs(execute(new File(context), command, envVars, "describe", "component", "-o", "json"));
    } else {
      return Collections.emptyList();
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
    JSonParser parser = new JSonParser(Serialization.json().readTree(json));
    return parser.parseDescribeComponentInfo(kind, isPodmanPresent());
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
      if (openshiftClient != null) {
        openshiftClient.routes().inNamespace(project).withLabelIn(KubernetesLabels.COMPONENT_LABEL, deployment).list()
          .getItems().forEach(route -> openshiftClient.routes().withName(route.getMetadata().getName())
            .withPropagationPolicy(DeletionPropagation.BACKGROUND).delete());
        openshiftClient.buildConfigs().inNamespace(project).withLabel(KubernetesLabels.COMPONENT_LABEL, deployment)
          .list().getItems().forEach(bc -> openshiftClient.buildConfigs().withName(bc.getMetadata().getName())
            .withPropagationPolicy(DeletionPropagation.BACKGROUND).delete());
        openshiftClient.imageStreams().inNamespace(project).withLabel(KubernetesLabels.COMPONENT_LABEL, deployment)
          .list().getItems().forEach(is -> openshiftClient.imageStreams().withName(is.getMetadata().getName())
            .withPropagationPolicy(DeletionPropagation.BACKGROUND).delete());
      }
    } catch (KubernetesClientException e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public void deleteComponent(String project, String context, String component,
                              ComponentKind kind) throws IOException {
    if (kind != ComponentKind.OTHER) {
      List<String> args = new ArrayList<>();
      args.add("delete");
      args.add("component");
      args.add("-f");
      if (context != null) {
        File dir = createWorkingDirectory(context);
        try {
          execute(dir, command, envVars, args.toArray(new String[0]));
        } catch (IOException e) {
          LOGGER.warn(e.getLocalizedMessage(), e);
        }
        FileUtil.delete(new File(dir, "devfile.yaml").toPath());
        FileUtil.delete(new File(dir, PLUGIN_FOLDER).toPath());
      } else {
        args.add("--namespace");
        args.add(project);
        args.add("--name");
        args.add(component);
        execute(command, envVars, args.toArray(new String[0]));
      }
    } else {
      deleteDeployment(project, component);
    }
  }

  private void doLog(String context, boolean follow, boolean deploy, String platform, List<ProcessHandler> handlers) throws IOException {
    int index = deploy ? 1 : 0;
    ProcessHandler handler = handlers.get(index);
    if (handler == null) {
      List<String> args = new ArrayList<>();
      args.add(command);
      args.add("logs");
      if (deploy) {
        args.add("--deploy");
      } else {
        args.add("--dev");
      }
      if (follow) {
        args.add("--follow");
      }
      if (!Strings.isEmptyOrSpaces(platform)) {
        args.add("--platform");
        args.add(platform);
      }
      ExecHelper.executeWithTerminal(
        this.project, WINDOW_TITLE,
        new File(context),
        false,
        envVars,
        null,
        null,
        new ProcessAdapter() {
          @Override
          public void startNotified(@NotNull ProcessEvent event) {
            handlers.set(index, event.getProcessHandler());
          }

          @Override
          public void processTerminated(@NotNull ProcessEvent event) {
            handlers.set(index, null);
          }
        },
        args.toArray(new String[0]));
    }
  }

  @Override
  public void follow(String context, boolean deploy, String platform, List<ProcessHandler> handlers) throws IOException {
    doLog(context, true, deploy, platform, handlers);
  }

  @Override
  public void log(String context, boolean deploy, String platform, List<ProcessHandler> handlers) throws IOException {
    doLog(context, false, deploy, platform, handlers);
  }

  @Override
  public boolean isLogRunning(String component, boolean deploy) {
    throw new UnsupportedOperationException();
  }

  @Nullable
  private File createWorkingDirectory(String context) {
    if (context != null) {
      return new File(context);
    }
    return null;
  }

  @Override
  public void createProject(String project) throws IOException {
    execute(command, envVars, "create", NAMESPACE_FIELD, project, "-w");
  }

  @Override
  public void deleteProject(String project) throws IOException {
    execute(command, envVars, "delete", NAMESPACE_FIELD, project, "-f", "-w");
    if (project.equals(currentNamespace)) {
      currentNamespace = null;
    }
  }

  @Override
  public void setProject(String project) throws IOException {
    execute(command, envVars, "set", NAMESPACE_FIELD, project);
  }

  @Override
  public boolean isAuthorized() {
    try {
      client.authorization().v1().getApiGroups();
      // retrieving api groups worked, we're authorized
      return true;
    } catch (KubernetesClientException e) {
      if (KubernetesClientExceptionUtils.isUnauthorized(e)) {
        // retrieving api groups didn't work, we're NOT authorized
        return false;
      } else if (KubernetesClientExceptionUtils.isForbidden(e)) {
        // retrieving api groups didn't work, but we're authorized
        return true;
      } else {
        throw e;
      }
    }
  }

  @Override
  public List<Component> getComponentsOnCluster(String project) throws IOException {
    return Serialization.configure(new ComponentDeserializer()).readValue(
      execute(command, envVars, "list", "--namespace", project, "-o", "json"),
      new TypeReference<>() {
      });
  }

  @Override
  public List<org.jboss.tools.intellij.openshift.utils.odo.Service> getServices(String project) throws IOException {
    try {
      return Serialization.configure(new ServiceDeserializer()).readValue(
        execute(command, envVars, "list", "service", "--namespace", project, "-o", "json"),
        new TypeReference<>() {
        });
    } catch (IOException e) {
      //https://github.com/openshift/odo/issues/5010
      if (e.getMessage().contains("\"no operator backed services found in namespace:") ||
        e.getMessage().contains("failed to list Operator backed services") ||
        e.getMessage().contains("Service Binding Operator is not installed")) {
        return Collections.emptyList();
      }
      throw e;
    }
  }

  @Override
  public void about() throws IOException {
    ExecHelper.executeWithTerminal(this.project, WINDOW_TITLE, true, envVars, command, "version");
  }

  private String generateBindingName(List<Binding> bindings) {
    int counter = 0;
    int finalCounter = counter;
    while (bindings.stream().anyMatch(binding -> binding.getName().equals("b" + finalCounter))) {
      counter++;
    }
    return "b" + counter;
  }

  @Override
  public Binding link(String context, String target) throws IOException {
    List<Binding> bindings = listBindings(context);
    String bindingName = generateBindingName(bindings);
    execute(new File(context), command, envVars, "add", "binding", "--name", bindingName, "--service",
      target, "--bind-as-files=false");
    return listBindings(context).stream().filter(b -> bindingName.equals(b.getName()))
      .findFirst()
      .orElse(null);
  }

  @Override
  public List<Binding> listBindings(String context) throws IOException {
    if (context != null) {
      return Serialization.configure(new BindingDeserializer()).readValue(
        execute(new File(context), command, envVars, "describe", "binding", "-o", "json"),
        new TypeReference<>() {
        });
    }
    return Collections.emptyList();
  }

  @Override
  public void deleteBinding(String context, String binding) throws IOException {
    execute(new File(context), command, envVars, "remove", "binding", "--name", binding);
  }

  @Override
  public void debug(String context, Integer port) throws IOException {
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
  public java.net.URL getMasterUrl() {
    return getMasterUrl(client);
  }

  private java.net.URL getMasterUrl(KubernetesClient client) {
    return client.getMasterUrl();
  }

  @Override
  public String consoleURL() throws IOException {
    try {
      if (openshiftClient != null) {
        VersionInfo info = openshiftClient.getOpenShiftV3Version();
        if (info == null) {
          ConfigMap configMap = openshiftClient.configMaps().inNamespace(OCP4_CONFIG_NAMESPACE).withName(OCP4_CONSOLE_PUBLIC_CONFIG_MAP_NAME).get();
          if (configMap != null) {
            return configMap.getData().get(OCP4_CONSOLE_URL_KEY_NAME);
          }
        } else {
          ConfigMap configMap = openshiftClient.configMaps().inNamespace(OCP3_CONFIG_NAMESPACE).withName(OCP3_WEBCONSOLE_CONFIG_MAP_NAME).get();
          String yaml = configMap.getData().get(OCP3_WEBCONSOLE_YAML_FILE_NAME);
          return Serialization.json().readTree(yaml).path("clusterInfo").path("consolePublicURL").asText();
        }
      }
      //https://<master-ip>:<apiserver-port>/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
      return client.getMasterUrl() + "console";
    } catch (KubernetesClientException e) {
      return client.getMasterUrl().toExternalForm();
    }
  }

  @Override
  public boolean isOpenShift() {
    return openshiftClient != null;
  }

  @Override
  public void migrateComponent(String name) {
    client.apps().deployments().withLabel(KubernetesLabels.COMPONENT_NAME_LABEL, name).delete();
  }

  @Override
  public List<ComponentDescriptor> discover(String path) throws IOException {
    return Serialization.configure(new ComponentDescriptorsDeserializer(new File(path).getAbsolutePath())).readValue(
      execute(new File(path), command, envVars, "list", "-o", "json"),
      new TypeReference<>() {
      });
  }

  @Override
  public ComponentTypeInfo getComponentTypeInfo(String componentType, String registryName) throws IOException {
    String json = execute(command, envVars, "registry", "list", "--devfile-registry", registryName, "--devfile", componentType, "-o", "json");
    JSonParser parser = new JSonParser(Serialization.json().readTree(json));
    return parser.parseComponentTypeInfo();
  }

  @Override
  public List<DevfileRegistry> listDevfileRegistries() throws IOException {
    return Serialization.configure(new DevfileRegistriesDeserializer()).readValue(
      execute(command, envVars, "preference", "view", "-o", "json"),
      new TypeReference<>() {
      });
  }

  @Override
  public void createDevfileRegistry(String name, String url, String token) throws IOException {
    if (!Strings.isEmptyOrSpaces(token)) {
      execute(command, envVars, "preference", "add", "registry", name, url, "--token", token);
    } else {
      execute(command, envVars, "preference", "add", "registry", name, url);
    }
  }

  @Override
  public void deleteDevfileRegistry(String name) throws IOException {
    execute(command, envVars, "preference", "remove", "registry", "-f", name);
  }

  @Override
  public List<DevfileComponentType> getComponentTypesFromRegistry(String name) throws IOException {
    return getAllComponentTypes().stream().
      filter(type -> name.equals(type.getDevfileRegistry().getName())).toList();
  }

  private boolean isPodmanPresent() {
    try {
      if (isPodmanPresent == null) {
        this.isPodmanPresent = processPodmanPresent(command);
      }
      return isPodmanPresent.get(2, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.warn(e.getLocalizedMessage(), e);
    } catch (ExecutionException | TimeoutException e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
    }
    return false;
  }

  private @NotNull CompletableFuture<Boolean> processPodmanPresent(String command) {
    return CompletableFuture
      .supplyAsync(() -> {
        try {
          ExecHelper.ExecResult result = ExecHelper.executeWithResult(command, false, new File(HOME_FOLDER), envVars, "version");
          return result.getStdOut().contains("unable to fetch the podman client version");
        } catch (IOException e) {
          LOGGER.warn(e.getLocalizedMessage(), e);
          return false;
        }
      }, runnable -> ApplicationManager.getApplication().executeOnPooledThread(runnable));
  }

  private static final class OpenShiftClientFactory implements Function<KubernetesClient, OpenShiftClient> {
    @Override
    public OpenShiftClient apply(KubernetesClient client) {
      OpenShiftClient osClient = client.adapt(OpenShiftClient.class);
      try {
        if (ClusterHelper.isOpenShift(osClient)) {
          return osClient;
        } else {
          return null;
        }
      } catch (KubernetesClientException e) {
        if (e.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
          // openshift but unauthorized
          return osClient;
        } else {
          return null;
        }
      }
    }
  }

}
