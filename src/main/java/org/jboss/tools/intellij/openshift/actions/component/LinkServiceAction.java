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
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.openshift.client.OpenShiftClient;
import me.snowdrop.servicecatalog.api.model.ServiceInstance;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LinkServiceAction extends OdoAction {
  public LinkServiceAction() {
    super(ComponentNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    OpenShiftClient client = ((ApplicationsRootNode)componentNode.getRoot()).getClient();
    CompletableFuture.runAsync(() -> {
      try {
        List<ServiceInstance> services = odo.getServices(client, projectNode.toString(), applicationNode.toString());
        if (!services.isEmpty()) {
          String service;
          if (services.size() == 1) {
            service = KubernetesLabels.getComponentName(services.get(0));
          } else {
            Object[] servicesArray = services.stream().map(KubernetesLabels::getComponentName).toArray();
            service = (String) UIHelper.executeInUI(() -> JOptionPane.showInputDialog(null, "Link service", "Select service", JOptionPane.QUESTION_MESSAGE, null, servicesArray, servicesArray[0]));
          }
          if (service != null) {
            odo.link(projectNode.toString(), applicationNode.toString(), componentNode.toString(), service, null);
            Notifications.Bus.notify(new Notification("OpenShift", "Link service", "Component linked to " + service,
            NotificationType.INFORMATION));
          }
       } else {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "No services to link to", "Link service", JOptionPane.WARNING_MESSAGE));
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Link service", JOptionPane.ERROR_MESSAGE));
      }
    });
  }
}
