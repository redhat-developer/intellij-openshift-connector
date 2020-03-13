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
import io.fabric8.servicecatalog.api.model.ServiceInstance;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.Constants.GROUP_DISPLAY_ID;

public class LinkServiceAction extends OdoAction {
  public LinkServiceAction() {
    super(ComponentNode.class);
  }

  @Override
  public boolean isVisible(Object selected) {
    boolean visible = super.isVisible(selected);
    if (visible) {
      visible = ((Component)((ComponentNode)selected).getUserObject()).getState() == ComponentState.PUSHED;
    }
    return visible;
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = (Component) componentNode.getUserObject();
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        List<ServiceInstance> services = odo.getServices(projectNode.toString(), applicationNode.toString());
        if (!services.isEmpty()) {
          String service;
          if (services.size() == 1) {
            service = KubernetesLabels.getComponentName(services.get(0));
          } else {
            String[] servicesArray = services.stream().map(KubernetesLabels::getComponentName).toArray(String[]::new);
            service = (String) UIHelper.executeInUI(() -> Messages.showEditableChooseDialog("Link service", "Select service", Messages.getQuestionIcon(), servicesArray, servicesArray[0], null));
          }
          if (service != null) {
            Notification notification = new Notification(GROUP_DISPLAY_ID, "Link service", "Linking component to service " + service, NotificationType.INFORMATION);
            Notifications.Bus.notify(notification);
            odo.link(projectNode.toString(), applicationNode.toString(), component.getName(), component.getPath(), service, null);
            notification.expire();
            Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "Link service", "Component linked to " + service,
            NotificationType.INFORMATION));
          }
       } else {
          UIHelper.executeInUI(() -> Messages.showWarningDialog("No services to link to", "Link service"));
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Link service"));
      }
    });
  }
}
