/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.twelvemonkeys.lang.Platform;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.OpenShiftClient;
import me.snowdrop.servicecatalog.api.client.ServiceCatalogClient;
import me.snowdrop.servicecatalog.api.model.ServiceInstance;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.ToolsConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Odo {
  private String command;

  private Odo() throws IOException {
    command = getCommand();
  }

  private static Odo INSTANCE;

  public static final Odo get() throws IOException {
    if (INSTANCE == null) {
      INSTANCE = new Odo();
    }
    return INSTANCE;
  }

  public String getCommand() throws IOException {
    if (command == null) {
      command = getOdoCommand();
    }
    return command;
  }

  private String getOdoCommand() throws IOException {
    ToolsConfig.Tool odoTool = ConfigHelper.loadToolsConfig().getTools().get("odo");
    ToolsConfig.Platform platform = odoTool.getPlatforms().get(Platform.os().id());
    String command = platform.getCmdFileName();
    try {
      Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      Path path = Paths.get(System.getProperty("user.home"), ".vs-openshift", "cache", odoTool.getVersion(), command);
      if (!Files.exists(path)) {
        final Path dlFilePath = path.resolveSibling(platform.getDlFileName());
        final String cmd = path.toString();
        if (JOptionPane.showConfirmDialog(null, "Odo not found, do you want to download odo ?") == JOptionPane.OK_OPTION) {
          command = ProgressManager.getInstance().run(new Task.WithResult<String, IOException>(null, "Downloading Odo", true) {
            @Override
            public String compute(@NotNull ProgressIndicator progressIndicator) throws IOException {
              OkHttpClient client = new OkHttpClient();
              Request request = new Request.Builder().url(platform.getUrl()).build();
              Response response = client.newCall(request).execute();
              downloadFile(response.body().byteStream(), dlFilePath, progressIndicator, response.body().contentLength());
              if (progressIndicator.isCanceled()) {
                throw new IOException("Interrupted");
              } else {
                uncompress(dlFilePath, cmd);
                return cmd;
              }
            }
          });
        }
      } else {
        command = path.toString();
      }
    }
    return command;
  }

  private void uncompress(Path dlFilePath, String cmd) throws IOException {
    try (InputStream input = new BufferedInputStream(Files.newInputStream(dlFilePath))) {
      try (CompressorInputStream stream = new CompressorStreamFactory().createCompressorInputStream(input)) {
        try (OutputStream output = new FileOutputStream(cmd)) {
          IOUtils.copy(stream, output);
        }
        if (!new File(cmd).setExecutable(true)) {
          throw new IOException("Can't set " + cmd + " as executable");
        }
      }
    } catch (CompressorException e) {
      throw new IOException(e);
    }
  }


  private static void downloadFile(InputStream input, Path dlFileName, ProgressIndicator progressIndicator, long size) throws IOException {
    byte[] buffer = new byte[4096];
    Files.createDirectories(dlFileName.getParent());
    try (OutputStream output = Files.newOutputStream(dlFileName)) {
      int lg;
      long accumulated = 0;
      while (((lg = input.read(buffer)) > 0) && !progressIndicator.isCanceled()) {
        output.write(buffer, 0, lg);
        accumulated += lg;
        progressIndicator.setFraction((double) accumulated / size);
      }
    }
  }

  public List<Project> getProjects(OpenShiftClient client) {
    return client.projects().list().getItems();
  }

  private static String execute(String command, String ...args) throws IOException {
    return ExecHelper.execute(command, args).replaceAll("/---[\\s\\S]*---/g", "");
  }
  public void createApplication(String project, String application) throws IOException {
    execute(command, "app", "create", application, "--project", project);
  }

  public void describeApplication(String project, String application) throws IOException {
    ExecHelper.executeWithTerminal(command, "app", "describe", application, "--project", project);
  }

  public void deleteApplication(String project, String application) throws IOException {
    execute(command, "app", "delete", application, "-f", "--project", project);
  }

  public void push(String project, String application, String component) throws IOException {
    ExecHelper.executeWithTerminal(command, "push", "--project", project, "--app", application, component);
  }

  public void describeComponent(String project, String application, String component) throws IOException {
    ExecHelper.executeWithTerminal(command, "describe", "--project", project, "--app", application, component);
  }

  public void watch(String project, String application, String component) throws IOException {
    ExecHelper.executeWithTerminal(command, "watch", "--project", project, "--app", application, component);
  }

  public void createComponentLocal(String project, String application, String componentType, String componentVersion, String component, String source) throws IOException {
    ExecHelper.executeWithTerminal(command, "create", componentType + ':' + componentVersion, component,
      "--local", source, "--project", project, "--app", application);

  }

  public void createComponentGit(String project, String application, String componentType, String componentVersion, String component, String source) throws IOException {
    ExecHelper.executeWithTerminal(command, "create", componentType + ':' + componentVersion, component,
      "--git", source, "--project", project, "--app", application);

  }

  public void createService(String project, String application, String serviceTemplate, String servicePlan, String service) throws IOException {
    ExecHelper.executeWithTerminal(command, "service", "create", serviceTemplate, "--plan", servicePlan, service, "--app", application, "--project", project);
  }

  public void deleteService(String project, String application, String service) throws IOException {
    execute(command, "service", "delete", "--project", project, "--app", application, service, "-f");
  }


  private ComponentType toComponentType(String[] line) {
    return new ComponentType() {
      @Override
      public String getName() {
        return line[0];
      }

      @Override
      public String getVersions() {
        return line[2];
      }
    };
  }

  public List<ComponentType> getComponentTypes() throws IOException {
    return loadList(execute(command, "catalog", "list", "components"), this::toComponentType);
  }

  private <T> List<T> loadList(String output, Function<String[], T> mapper) throws IOException {
    try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
      return reader.lines().skip(1).map(s -> s.replaceAll("\\s{1,}", "|"))
        .map(s -> s.split("\\|"))
        .map(mapper)
        .collect(Collectors.toList());
    }
  }

  private ServiceTemplate toServiceTemplate(String[] line) {
    return new ServiceTemplate() {
      @Override
      public String getName() {
        return line[0];
      }

      @Override
      public String getPlan() {
        return line[1];
      }
    };
  }

  public List<ServiceTemplate> getServiceTemplates() throws IOException {
    return loadList(execute(command, "catalog", "list", "services"), this::toServiceTemplate);
  }

  public List<Integer> getServicePorts(OpenShiftClient client, String project, String application, String component) {
    Service service = client.services().inNamespace(project).withName(component + '-' + application).get();
    return service.getSpec().getPorts().stream().map(ServicePort::getPort).collect(Collectors.toList());
  }

  public void createUrl(String project, String application, String component, Integer port) throws IOException {
    execute(command, "url", "create", "--project", project, "--app", application, "--component", component, "--port", port.toString());
  }

  public void deleteComponent(String project, String application, String component) throws IOException {
    execute(command, "delete", "--project", project, "--app", application, component, "-f");
  }

  public void follow(String project, String application, String component) throws IOException {
    ExecHelper.executeWithTerminal(command, "log", "--project", project, "--app", application, component, "-f");
  }

  public void log(String project, String application, String component) throws IOException {
    ExecHelper.executeWithTerminal(command, "log", "--project", project, "--app", application, component);
  }


  public void createProject(String project) throws IOException {
    execute(command, "project", "create", project);
  }

  public void deleteProject(String project) throws IOException {
    execute(command, "project", "delete", project, "-f");
  }

  public void login(String url, String userName, char[] password, String token) throws IOException {
    if (token == null || token.isEmpty()) {
      execute(command, "login", url, "-u", userName, "-p", String.valueOf(password), " --insecure-skip-tls-verify");
    } else {
      execute(command, "login", url, "-t", token, " --insecure-skip-tls-verify");
    }
  }

  public void logout() throws IOException {
    execute(command, "logout");
  }

  public List<OdoConfig.Application> getApplication(String project) throws IOException {
    return ConfigHelper.loadOdoConfig().getActiveApplications().stream().filter(a -> a.getProject().equals(project)).collect(Collectors.toList());
  }

  public List<DeploymentConfig> getComponents(OpenShiftClient client, String project, String application) {
    return client.deploymentConfigs().inNamespace(project).withLabelSelector(new LabelSelectorBuilder().addToMatchLabels(KubernetesLabels.APP_LABEL, application).build()).list().getItems();
  }

  public List<ServiceInstance> getServices(OpenShiftClient client, String project, String application) {
    ServiceCatalogClient sc = client.adapt(ServiceCatalogClient.class);
    return sc.serviceInstances().inNamespace(project).withLabelSelector(new LabelSelectorBuilder().addToMatchLabels(KubernetesLabels.APP_LABEL, application).build()).list().getItems();
  }

  protected LabelSelector getLabelSelector(String application, String component) {
    return new LabelSelectorBuilder().addToMatchLabels(KubernetesLabels.APP_LABEL, application)
      .addToMatchLabels(KubernetesLabels.COMPONENT_NAME_LABEL, component)
      .build();
  }


  public List<PersistentVolumeClaim> getStorages(OpenShiftClient client, String project, String application, String component) {
    return client.persistentVolumeClaims().inNamespace(project).withLabelSelector(getLabelSelector(application, component)).list().getItems()
            .stream().filter(pvc -> pvc.getMetadata().getLabels().containsKey(KubernetesLabels.STORAGE_NAME_LABEL)).collect(Collectors.toList());

  }

  public void listComponents() throws IOException {
    ExecHelper.executeWithTerminal(command, "catalog", "list", "components");
  }

  public void listServices() throws IOException {
    ExecHelper.executeWithTerminal(command, "catalog", "list", "services");
  }

  public void about() throws IOException {
    ExecHelper.executeWithTerminal(command, "version");
  }

  public void createStorage(String project, String application, String component, String name, String mountPath, String storageSize) throws IOException {
    execute(command, "storage", "create", "--project", project, "--app", application, "--component", component, name, "--path", mountPath, "--size", storageSize);
  }

  public void deleteStorage(String project, String application, String component, String storage) throws IOException {
    execute(command, "storage", "delete", "--project", project, "--app", application, "--component", component, storage, "-f");
  }

  public void link(String project, String application, String component, String source) throws IOException {
    execute(command, "link", source, "--project", project, "--app", application, "--component", component);
  }
}
