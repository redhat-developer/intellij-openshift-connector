package org.jboss.tools.intellij.openshift.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.twelvemonkeys.lang.Platform;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.OpenShiftClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class OdoHelper {
  private String command;

  private OdoHelper() throws IOException {
    command = getCommand();
  }

  private static OdoHelper INSTANCE;

  public static final OdoHelper get() throws IOException {
    if (INSTANCE == null) {
      INSTANCE = new OdoHelper();
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

  public void createApplication(String project, String application) throws IOException {
    ExecHelper.execute(command, "app", "create", application, "--project", project);
  }

  public void deleteApplication(String project, String application) throws IOException {
    ExecHelper.execute(command, "app", "delete", application, "-f", "--project", project);
  }

  public void push(String project, String application, String component) throws IOException {
    ExecHelper.executeWithTerminal(command, "push", "--project", project, "--app", application, component);
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

  public List<String[]> getComponentTypes() throws IOException {
    return loadComponentTypes(ExecHelper.execute(command, "catalog", "list", "components"));
  }

  private List<String[]> loadComponentTypes(String output) throws IOException {
    try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
      return reader.lines().skip(1).map(s -> s.replaceAll("\\s{1,}", "|"))
        .map(s -> s.split("\\|"))
        .map(s -> new String[] {s[0], s[2]})
        .collect(Collectors.toList());
    }
  }

  public List<Integer> getServicePorts(OpenShiftClient client, String project, String application, String component) {
    Service service = client.services().inNamespace(project).withName(component + '-' + application).get();
    return service.getSpec().getPorts().stream().map(ServicePort::getPort).collect(Collectors.toList());
  }

  public void createUrl(String project, String application, String component, Integer port) throws IOException {
    ExecHelper.execute(command, "url", "create", "--project", project, "--app", application, "--component", component, "--port", port.toString());
  }

  public void deleteComponent(String project, String application, String component) throws IOException {
    ExecHelper.execute(command, "delete", "--project", project, "--app", application, component, "-f");
  }

  public void follow(String project, String application, String component) throws IOException {
    ExecHelper.executeWithTerminal(command, "log", "--project", project, "--app", application, component, "-f");
  }

  public void log(String project, String application, String component) throws IOException {
    ExecHelper.executeWithTerminal(command, "log", "--project", project, "--app", application, component);
  }


  public void createProject(String project) throws IOException {
    ExecHelper.execute(command, "project", "create", project);
  }

  public void deleteProject(String project) throws IOException {
    ExecHelper.execute(command, "project", "delete", project, "-f");

  }
}
