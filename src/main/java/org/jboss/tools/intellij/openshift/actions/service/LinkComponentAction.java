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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LinkComponentAction extends OdoAction {
  public LinkComponentAction() {
    super(ServiceNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ServiceNode serviceNode = (ServiceNode) selected;
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    OpenShiftClient client = ((ApplicationsRootNode)serviceNode.getRoot()).getClient();
    CompletableFuture.runAsync(() -> {
      try {
        List<DeploymentConfig> components = odo.getComponents(client, projectNode.toString(), applicationNode.toString());
        if (!components.isEmpty()) {
          String component;
          if (components.size() == 1) {
            component = KubernetesLabels.getComponentName(components.get(0));
          } else {
            Object[] componentsArray = components.stream().map(comp -> KubernetesLabels.getComponentName(comp)).toArray();
            component = (String) UIHelper.executeInUI(() -> JOptionPane.showInputDialog(null, "Link component", "Select component", JOptionPane.QUESTION_MESSAGE, null, componentsArray, componentsArray[0]));
          }
          if (component != null) {
            odo.link(projectNode.toString(), applicationNode.toString(), component, serviceNode.toString(), null);
            Notifications.Bus.notify(new Notification("OpenShift", "Link component", "Service linked to " + component,
            NotificationType.INFORMATION));
          }
       } else {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "No components to link to", "Link component", JOptionPane.WARNING_MESSAGE));
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Link component", JOptionPane.ERROR_MESSAGE));
      }
    });
  }
}
