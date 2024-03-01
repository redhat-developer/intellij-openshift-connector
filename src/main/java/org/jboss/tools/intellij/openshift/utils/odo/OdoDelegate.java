package org.jboss.tools.intellij.openshift.utils.odo;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessHandler;

import java.io.IOException;
import java.util.List;

public interface OdoDelegate extends Odo {

  void start(String context, ComponentFeature feature, ProcessHandler handler, ProcessAdapter processAdapter) throws IOException;

  void stop(String context, ComponentFeature feature, ProcessHandler handler) throws IOException;

  void follow(String context, boolean deploy, String platform, List<ProcessHandler> handlers) throws IOException;

  void log(String context, boolean deploy, String platform, List<ProcessHandler> handlers) throws IOException;

}
