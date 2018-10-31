package org.jboss.tools.intellij.openshift.utils;

import org.apache.commons.exec.*;
import org.apache.xmlgraphics.util.WriterOutputStream;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ExecHelper {
  public static CompletableFuture<String> execute(String executable, String... arguments) {
    DefaultExecutor executor = new DefaultExecutor();
    StringWriter writer = new StringWriter();
    PumpStreamHandler handler = new PumpStreamHandler(new WriterOutputStream(writer));
    executor.setStreamHandler(handler);
    CommandLine command = new CommandLine(executable).addArguments(arguments);
    try {
      executor.execute(command);
      return CompletableFuture.completedFuture(writer.toString());
    } catch (IOException e) {
      return CompletableFuture.completedFuture("").thenApply((s) -> {
        throw new CompletionException("Failed to launch '" + command + "'", e);
      });
    }
  }
}
