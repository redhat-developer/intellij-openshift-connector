package org.jboss.tools.intellij.openshift.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.twelvemonkeys.lang.Platform;
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
import java.nio.file.Paths;
import java.util.List;

public class OdoHelper {
  private String command;

  private OdoHelper() {
  }

  public static final OdoHelper INSTANCE = new OdoHelper();

  public String getCommand() throws IOException {
    if (command == null) {
      command = getOdoCommand();
    }
    return command;
  }

  private String getOdoCommand() throws IOException {
    ToolsConfig.Platform platform = ConfigHelper.loadToolsConfig().getTools().get("odo").getPlatforms().get(Platform.os().id());
    String command = platform.getCmdFileName();
    try {
      Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      command = System.getProperty("user.home") + File.separatorChar + ".vs-openshift" + File.separatorChar + command;
      if (!Files.exists(Paths.get(command))) {
        final String dlFileName = System.getProperty("user.home") + File.separatorChar + ".vs-openshift" + File.separatorChar + platform.getDlFileName();
        final String cmd = command;
        if (JOptionPane.showConfirmDialog(null, "Odo not found, do you want to download odo ?") == JOptionPane.OK_OPTION) {
          ProgressManager.getInstance().run(new Task.WithResult<String, IOException>(null, "Downloading Odo", true) {
            @Override
            public String compute(@NotNull ProgressIndicator progressIndicator) throws IOException {
              OkHttpClient client = new OkHttpClient();
              Request request = new Request.Builder().url(platform.getUrl()).build();
              Response response = client.newCall(request).execute();
              downloadFile(response.body().byteStream(), dlFileName, progressIndicator, response.body().contentLength());
              if (progressIndicator.isCanceled()) {
                throw new IOException("Interrupted");
              } else {
                uncompress(dlFileName, cmd);
                return cmd;
              }
            }
          });
        }
      }
    }
    return command;
  }

  private void uncompress(String dlFileName, String cmd) throws IOException {
    try (InputStream input = new BufferedInputStream(new FileInputStream(dlFileName))) {
      try (CompressorInputStream stream = new CompressorStreamFactory().createCompressorInputStream(input)) {
        try (OutputStream output = new FileOutputStream(cmd)) {
          IOUtils.copy(stream, output);
        }
      }
    } catch (CompressorException e) {
      throw new IOException(e);
    }
  }


  private static void downloadFile(InputStream input, String dlFileName, ProgressIndicator progressIndicator, long size) throws IOException {
    byte[] buffer = new byte[4096];
    new File(dlFileName).getParentFile().mkdirs();
    try (OutputStream output = new FileOutputStream(dlFileName)) {
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
}
