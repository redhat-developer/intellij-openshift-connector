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

import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class LinkComponentAction extends OdoAction {

  public LinkComponentAction() {
    super(ComponentNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "link component to component";}

  protected String getSelectedTargetComponent(OdoFacade odo, String project, String component) throws IOException {
    String targetComponent = null;

    List<Component> components = odo.getComponents(project)
      .stream().filter(comp -> !comp.getName().equals(component)).collect(Collectors.toList());
    if (!components.isEmpty()) {
      if (components.size() == 1) {
        targetComponent = components.get(0).getName();
      } else {
        String[] componentsArray = components.stream().map(Component::getName).toArray(String[]::new);
        targetComponent = UIHelper.executeInUI(() ->
          Messages.showEditableChooseDialog(
            "Select component",
            "Link Component",
            Messages.getQuestionIcon(),
            componentsArray,
            componentsArray[0],
            null));
      }
    }
    return targetComponent;
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component sourceComponent = componentNode.getComponent();
    NamespaceNode namespaceNode = componentNode.getParent();
    runWithProgress((ProgressIndicator progress) -> {
        try {
          setProcessing("Linking Component...", namespaceNode);
          String targetComponent = getSelectedTargetComponent(odo, namespaceNode.getName(), sourceComponent.getName());
          linkComponent(targetComponent, sourceComponent, odo);
          clearProcessing(namespaceNode);
        } catch (IOException e) {
          clearProcessing(namespaceNode);
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Link Component"));
        }
      },
      "Link Component...",
      getEventProject(anActionEvent));
  }

  private void linkComponent(String targetComponent, Component sourceComponent, Odo odo) throws IOException {
    if (targetComponent != null) {
      Notification notification = NotificationUtils.notifyInformation("Link Component", "Linking component to " + targetComponent);
      odo.link(sourceComponent.getPath(), targetComponent);
      notification.expire();
      NotificationUtils.notifyInformation("Link Component", "Component linked to " + targetComponent);
      sendTelemetryResults(TelemetryResult.SUCCESS);
    } else {
      String message = "No components to link to";
      sendTelemetryError(message);
      UIHelper.executeInUI(() -> Messages.showWarningDialog(message, "Link Component"));
    }
  }
}
