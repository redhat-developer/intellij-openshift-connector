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
package org.jboss.tools.intellij.openshift.utils;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.SystemInfo;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.xmlgraphics.util.WriterOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;
import org.jetbrains.plugins.terminal.TerminalOptionsProvider;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

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

  private static class RedirectedStream extends FilterInputStream {
    private boolean emitLF = false;

    private RedirectedStream(InputStream delegate) {
      super(delegate);
    }

    @Override
    public synchronized int read() throws IOException {
      if (emitLF) {
        emitLF = false;
        return '\n';
      } else {
        int c = super.read();
        if (c == '\n') {
          emitLF = true;
          c = '\r';
        }
        return c;
      }
      //return super.read();
    }

    @Override
    public synchronized int read(@NotNull byte[] b) throws IOException {
      return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(@NotNull byte[] b, int off, int len) throws IOException {
      if (b == null) {
        throw new NullPointerException();
      } else if (off < 0 || len < 0 || len > b.length - off) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return 0;
      }

      int c = read();
      if (c == -1) {
        return -1;
      }
      b[off] = (byte)c;

      int i = 1;
      try {
        for (; i < len  && available() > 0; i++) {
          c = read();
          if (c == -1) {
            break;
          }
          b[off + i] = (byte)c;
        }
      } catch (IOException ee) {}
      return i;
    }
  }
  private static class RedirectedProcess extends Process {
    private final Process delegate;
    private final InputStream inputStream;
    private final InputStream errorStream;

    private RedirectedProcess(Process delegate) {
      this.delegate = delegate;
      inputStream = new RedirectedStream(delegate.getInputStream()) {};
      errorStream = new RedirectedStream(delegate.getErrorStream()) {};
    }

    @Override
    public OutputStream getOutputStream() {
      return delegate.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
      return inputStream;
    }

    @Override
    public InputStream getErrorStream() {
      return errorStream;
    }

    @Override
    public int waitFor() throws InterruptedException {
      return delegate.waitFor();
    }

    @Override
    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
      return delegate.waitFor(timeout, unit);
    }

    @Override
    public int exitValue() {
      return delegate.exitValue();
    }

    @Override
    public void destroy() {
      delegate.destroy();
    }

    @Override
    public Process destroyForcibly() {
      return delegate.destroyForcibly();
    }

    @Override
    public boolean isAlive() {
      return delegate.isAlive();
    }
  }

  public static void executeWithTerminal(String... command) throws IOException {
      try {
        Process p = new ProcessBuilder(command).start();
        if (SystemInfo.isWindows) {
          p = new RedirectedProcess(p);
        }

        final Process process = p;
        AbstractTerminalRunner runner = new AbstractTerminalRunner(ProjectManager.getInstance().getDefaultProject()) {
          @Override
          protected Process createProcess(@Nullable String s) {
            return process;
          }

          @Override
          protected ProcessHandler createProcessHandler(Process process) {
            return null;
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
                return process.isAlive();
              }
            };
          }

          @Override
          public String runningTargetName() {
            return null;
          }
        };
        TerminalOptionsProvider terminalOptions = ServiceManager.getService(TerminalOptionsProvider.class);
        boolean previousAutoClassClose = terminalOptions.closeSessionOnLogout();
        terminalOptions.setCloseSessionOnLogout(false);
        ApplicationManager.getApplication().invokeLater(() -> TerminalView.getInstance(ProjectManager.getInstance().getOpenProjects()[0]).createNewSession(ProjectManager.getInstance().getOpenProjects()[0], runner));
        terminalOptions.setCloseSessionOnLogout(previousAutoClassClose);
        if (p.waitFor() != 0) {
          throw new IOException("Process returned exit code: " + p.exitValue(), null);
        }
    } catch (IOException e) {
        throw e;
      }
      catch (InterruptedException e) {
        throw new IOException(e);
      }
  }
}
