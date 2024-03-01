package org.jboss.tools.intellij.openshift.utils.odo;

import java.io.IOException;
import java.util.function.Consumer;

public interface OdoFacade extends Odo {

  void start(String context, String component, ComponentFeature feature,
             Consumer<Boolean> callback, Consumer<Boolean> processTerminatedCallback) throws IOException;

  void stop(String context, String component, ComponentFeature feature) throws IOException;

  void follow(String context, String component, boolean deploy, String platform) throws IOException;

  void log(String context, String component, boolean deploy, String platform) throws IOException;

}
