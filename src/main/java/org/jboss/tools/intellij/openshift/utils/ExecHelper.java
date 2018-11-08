package org.jboss.tools.intellij.openshift.utils;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.ProjectManager;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalMode;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.ui.TerminalPanelListener;
import com.jediterm.terminal.ui.TerminalSession;
import com.jediterm.terminal.ui.TerminalWidget;
import org.apache.commons.exec.*;
import org.apache.xmlgraphics.util.WriterOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.TerminalView;

import javax.swing.JComponent;
import java.awt.Dimension;
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

          @Override
          public void openSession(@NotNull TerminalWidget terminal) {
            super.openSession(new TerminalWidget() {
              @Override
              public TerminalSession createTerminalSession(TtyConnector ttyConnector) {
                TerminalSession session = terminal.createTerminalSession(ttyConnector);
                if (session.getTerminal() instanceof JediTerminal) {
                  ((JediTerminal)session.getTerminal()).setModeEnabled(TerminalMode.AutoNewLine, true);
                }
                return session;
              }

              @Override
              public JComponent getComponent() {
                return terminal.getComponent();
              }

              @Override
              public boolean canOpenSession() {
                return terminal.canOpenSession();
              }

              @Override
              public void setTerminalPanelListener(TerminalPanelListener terminalPanelListener) {
                terminal.setTerminalPanelListener(terminalPanelListener);
              }

              @Override
              public Dimension getPreferredSize() {
                return terminal.getPreferredSize();
              }

              @Override
              public TerminalSession getCurrentSession() {
                return terminal.getCurrentSession();
              }

              @Override
              public TerminalDisplay getTerminalDisplay() {
                return terminal.getTerminalDisplay();
              }
            });
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
