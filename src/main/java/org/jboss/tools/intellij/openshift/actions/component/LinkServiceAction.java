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
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.Constants.GROUP_DISPLAY_ID;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class LinkServiceAction extends OdoAction {
  public LinkServiceAction() {
    super(ComponentNode.class);
  }

  @Override
  protected String getTelemetryActionName() { return "link component to service"; }

  @Override
  public boolean isVisible(Object selected) {
    boolean visible = super.isVisible(selected);
    if (visible) {
      Component comp = ((ComponentNode)selected).getComponent();
      visible = (comp.getState() == ComponentState.PUSHED);
    }
    return visible;
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = componentNode.getComponent();
    ApplicationNode applicationNode = componentNode.getParent();
    NamespaceNode namespaceNode = applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        List<Service> services = odo.getServices(namespaceNode.getName(), applicationNode.getName());
        if (!services.isEmpty()) {
          String service;
          if (services.size() == 1) {
            service = services.get(0).getName();
          } else {
            String[] servicesArray = services.stream().map(Service::getName).toArray(String[]::new);
            service = UIHelper.executeInUI(() -> Messages.showEditableChooseDialog("Link service", "Select service", Messages.getQuestionIcon(), servicesArray, servicesArray[0], null));
          }
          if (service != null) {
            Notification notification = new Notification(GROUP_DISPLAY_ID, "Link service", "Linking component to service " + service, NotificationType.INFORMATION);
            Notifications.Bus.notify(notification);
            odo.link(namespaceNode.getName(), applicationNode.getName(), component.getName(), component.getPath(), service, null);
            notification.expire();
            Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "Link service", "Component linked to " + service,
            NotificationType.INFORMATION));
            sendTelemetryResults(TelemetryResult.SUCCESS);
          }
       } else {
          String message = "No services to link to";
          sendTelemetryError(message);
          UIHelper.executeInUI(() -> Messages.showWarningDialog(message, "Link service"));
        }
      } catch (IOException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Link service"));
      }
    });
  }
}
