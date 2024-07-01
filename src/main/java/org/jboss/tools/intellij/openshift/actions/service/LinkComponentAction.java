/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.service;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class LinkComponentAction extends OdoAction {
  public LinkComponentAction() {
    super(ServiceNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "link service to component";}

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    ServiceNode serviceNode = (ServiceNode) selected;
    NamespaceNode namespaceNode = serviceNode.getParent();
    runWithProgress((ProgressIndicator progress) -> {
        try {
          setProcessing("Linking Component...", namespaceNode);
          List<Component> components = getTargetComponents(odo, namespaceNode.getName());
          if (!components.isEmpty()) {
            Component component = getComponent(components);
            if (component != null) {
              odo.link(component.getPath(), serviceNode.getName());
              NotificationUtils.notifyInformation("Link component", "Service linked to " + component.getName());
              sendTelemetryResults(TelemetryResult.SUCCESS);
            } else {
              sendTelemetryResults(TelemetryResult.ABORTED);
            }
          } else {
            String message = "No components to link to";
            sendTelemetryError(message);
            UIHelper.executeInUI(() -> Messages.showWarningDialog(message, "Link Component"));
          }
        } catch (IOException e) {
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Link Component"));
        } finally {
          clearProcessing(namespaceNode);
        }
      },
      "Link Component...",
      getEventProject(anActionEvent)
    );
  }

  private Component getComponent(List<Component> components) {
    if (components.size() == 1) {
      return components.get(0);
    } else {
      String[] componentNames = components.stream().map(Component::getName).toArray(String[]::new);
      String componentName = UIHelper.executeInUI(() -> Messages.showEditableChooseDialog(
        "Link component",
        "Select Component",
        Messages.getQuestionIcon(),
        componentNames,
        componentNames[0],
        null));
      return components.get(Arrays.asList(componentNames).indexOf(componentName));
    }
  }

  private List<Component> getTargetComponents(OdoFacade odo, String project) throws IOException {
    return odo.getComponents(project).stream().filter(component -> component.getLiveFeatures().isOnCluster()).collect(Collectors.toList());
  }
}
