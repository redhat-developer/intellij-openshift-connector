package org.jboss.tools.intellij.openshift.utils;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.ProjectManager;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import org.apache.commons.exec.*;
import org.apache.xmlgraphics.util.WriterOutputStream;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ExecHelper {
  public static String execute(String executable, String... arguments) throws IOException {
    DefaultExecutor executor = new DefaultExecutor();
    StringWriter writer = new StringWriter();
    PumpStreamHandler handler = new PumpStreamHandler(new WriterOutputStream(writer));
    executor.setStreamHandler(handler);
    CommandLine command = new CommandLine(executable).addArguments(arguments);
      executor.execute(command);
      return writer.toString();
  }

  public static CompletableFuture<Void> executeWithTerminal(String... command) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        OSProcessHandler handler = new OSProcessHandler(new GeneralCommandLine(command));
        AbstractTerminalRunner runner = new AbstractTerminalRunner(ProjectManager.getInstance().getDefaultProject()) {
          @Override
          protected Process createProcess(@Nullable String s) {
            return handler.getProcess();
          }

          @Override
          protected ProcessHandler createProcessHandler(Process process) {
            return handler;
          }

          @Override
          protected String getTerminalConnectionName(Process process) {
            return null;
          }

          @Override
          protected TtyConnector createTtyConnector(Process process) {
            return new ProcessTtyConnector(process, StandardCharsets.UTF_8) {
              @Override
              protected void resizeImmediately() {

              }

              @Override
              public String getName() {
                return "Odo";
              }

              @Override
              public boolean isConnected() {
                return true;
              }
            };
          }

          @Override
          public String runningTargetName() {
            return null;
          }
        };
        ApplicationManager.getApplication().invokeLater(() -> TerminalView.getInstance(ProjectManager.getInstance().getOpenProjects()[0]).createNewSession(ProjectManager.getInstance().getOpenProjects()[0], runner));
        handler.startNotify();
        handler.waitFor();
        if (handler.getProcess().exitValue() != 0) {
          throw new CompletionException("Process returned exit code: " + handler.getProcess().exitValue(), null);
        }
        return (Void)null;
      } catch (ExecutionException e) {
        throw new CompletionException(e);
      }
    });
  }
}
