/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.impl.ExecutionManagerImpl;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PROP_DEBUG_COMPONENT_LANGUAGE;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public abstract class DebugComponentAction extends ContextAwareComponentAction {

  private static final Logger LOG = LoggerFactory.getLogger(DebugComponentAction.class);

  private RunnerAndConfigurationSettings runSettings;

  private ExecutionEnvironment environment;

  @Override
  public boolean isVisible(Object selected) {
    boolean visible = super.isVisible(selected);
    if (visible) {
      ComponentNode componentNode = (ComponentNode) selected;
      Component component = componentNode.getComponent();
      return (isDebugRunning(component) && isDebuggable(component.getInfo()));
    }
    return false;
  }

  private boolean isDebugRunning(Component component) {
    return component.getLiveFeatures().isDebug();
  }

  @Override
  public String getTelemetryActionName() {
    return "debug component";
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      sendTelemetryResults(TelemetryResult.ABORTED);
      return;
    }
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = componentNode.getComponent();
    if (component.getLiveFeatures().isDebug()) {
      RunManager runManager = RunManager.getInstance(project);
      final Optional<Integer> port = createOrUpdateConfiguration(
        odo,
        runManager,
        component);
      port.ifPresent(portNumber -> executeDebug());
    }
  }

  private void executeDebug() {
    telemetrySender.addProperty(PROP_DEBUG_COMPONENT_LANGUAGE, getDebugLanguage().toLowerCase());
    ExecHelper.submit(() -> {
      // check if local debugger process is already running.
      if (ExecutionManagerImpl.isProcessRunning(getEnvironment().getContentToReuse())) {
        UIHelper.executeInUI(() ->
          Messages.showMessageDialog(
            "'" + runSettings.getName() + "' is a single-instance run configuration "
              + "and already running.",
            "Process '" + runSettings.getName() + "' is already running",
            Messages.getInformationIcon()));
        return;
      }
      // if debugger not running, run the debug config
      ApplicationManager.getApplication().invokeLater(
        () -> {
          try {
            Objects.requireNonNull(ProgramRunner.getRunner(
              DefaultDebugExecutor.getDebugExecutorInstance().getId(),
              runSettings.getConfiguration())).execute(getEnvironment());
            sendTelemetryResults(TelemetryResult.SUCCESS);
          } catch (ExecutionException e) {
            sendTelemetryError(e);
            LOG.error(e.getLocalizedMessage(), e);
          }
        });
    });

  }

  private Optional<Integer> createOrUpdateConfiguration(Odo odo, RunManager runManager, Component component) {
    ConfigurationType configurationType = getConfigurationType();
    String configurationName = component.getName() + " Remote Debug";
    try {
      Optional<Integer> port = getPort(odo, component);
      if (port.isPresent()) {
        //lookup if existing config already exist, based on name and type
        runSettings = runManager.findConfigurationByTypeAndName(
          configurationType.getId(), configurationName);
        if (runSettings == null) {
          // no run configuration found, create one and assign the selected port
          runSettings = runManager.createConfiguration(
            configurationName, configurationType.getConfigurationFactories()[0]);
          // also reset environment
          environment = null;
          runSettings.getConfiguration().setAllowRunningInParallel(false);
          runManager.addConfiguration(runSettings);
        }
        initConfiguration(runSettings.getConfiguration(), port.get());
        runManager.setSelectedConfiguration(runSettings);
        return port;
      }
    } catch (IOException e) {
      LOG.warn(e.getLocalizedMessage(), e);
    }
    return Optional.empty();
  }

  private static Optional<Integer> getPort(Odo odo, Component component) throws IOException {
    Optional<Integer> port = Optional.empty();
    List<URL> urls = odo.listURLs(component.getPath());
    String[] ports = urls.stream()
      .map(URL::getContainerPort)
      .toArray(String[]::new);
    if (ports.length == 1) {
      port = Optional.of(Integer.parseInt(urls.get(0).getLocalPort()));
    } else if (ports.length > 1) {
      String selected = UIHelper.executeInUI(() ->
        Messages.showEditableChooseDialog(
          "The component " + component.getName() + " has several ports to connect to,\nchoose the one the debugger will connect to",
          "Choose Debugger Port",
          Messages.getQuestionIcon(),
          ports,
          ports[0],
          null));
      //get the local mapped port
      Optional<String> remotePort = urls.stream()
        .filter(url -> url.getContainerPort().equals(selected))
        .map(URL::getLocalPort).findFirst();
      port = Optional.of(Integer.parseInt(remotePort.orElseThrow()));
    }
    return port;
  }

  private ExecutionEnvironment getEnvironment() {
    if (environment == null) {
      try {
        environment = ExecutionEnvironmentBuilder.create(
          DefaultDebugExecutor.getDebugExecutorInstance(), runSettings).build();
      } catch (ExecutionException e) {
        telemetrySender.error(e);
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return environment;
  }

  protected abstract boolean isDebuggable(@NotNull ComponentInfo componentInfo);

  protected abstract String getDebugLanguage();

  protected abstract ConfigurationType getConfigurationType();

  protected abstract void initConfiguration(RunConfiguration configuration, Integer port);

}
