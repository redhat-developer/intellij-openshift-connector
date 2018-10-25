package org.jboss.tools.intellij.openshift.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.twelvemonkeys.lang.Platform;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class OdoHelper {
  private String command;

  private OdoHelper() {
  }

  public static final OdoHelper INSTANCE = new OdoHelper();

  public CompletableFuture<String> getCommand() {
    if (command != null) {
      return CompletableFuture.completedFuture(command);
    } else {
      return getOdoCommand().thenApply(s -> command = s);
    }
  }

  private CompletableFuture<String> getOdoCommand() {
    try {
      ToolsConfig.Platform platform = ConfigHelper.loadToolsConfig().getTools().get("odo").getPlatforms().get(Platform.os().id());
      String command = platform.getCmdFileName();
      try {
        Runtime.getRuntime().exec(command);
      } catch (IOException e) {
        command = System.getProperty("user.home") + File.separatorChar + ".vs-openshift" + File.separatorChar + command;
        if (!Files.exists(Paths.get(command))) {
          final String cmd = command;
          if (JOptionPane.showConfirmDialog(null, "Odo not found, do you want to download odo ?") == JOptionPane.OK_OPTION) {
            ProgressManager.getInstance().run(new Task.WithResult<String, IOException>(null, "Downloading Odo", true) {
              @Override
              public String compute(@NotNull ProgressIndicator progressIndicator) throws IOException {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(platform.getUrl()).build();
                Response response = client.newCall(request).execute();
                downloadFile(response.body().byteStream(), cmd, progressIndicator, response.body().contentLength());
                if (progressIndicator.isCanceled()) {
                  throw new IOException("Interrupted");
                } else {
                  return cmd;
                }
              }
            });
          }
        }
      }
      return CompletableFuture.completedFuture(command);

    } catch (IOException e) {
      CompletableFuture<String> future = CompletableFuture.completedFuture(null);
      return future.thenApply(s -> {throw new CompletionException(e);});
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
}
