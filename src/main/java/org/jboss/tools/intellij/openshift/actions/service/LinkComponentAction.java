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
import com.intellij.openapi.ui.Messages;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        List<Component> components = getTargetComponents(odo, client, projectNode.toString(), applicationNode.toString());
        if (!components.isEmpty()) {
          Component component;
          if (components.size() == 1) {
            component = components.get(0);
          } else {
            String[] componentNames = components.stream().map(Component::getName).toArray(String[]::new);
            String componentName = (String) UIHelper.executeInUI(() -> Messages.showEditableChooseDialog("Link component", "Select component", Messages.getQuestionIcon(), componentNames, componentNames[0], null));
            component = components.get(Arrays.asList(componentNames).indexOf(componentName));
          }
          if (component != null) {
            odo.link(projectNode.toString(), applicationNode.toString(), component.getName(), component.getPath(), serviceNode.toString(), null);
            Notifications.Bus.notify(new Notification("OpenShift", "Link component", "Service linked to " + component,
            NotificationType.INFORMATION));
          }
       } else {
          UIHelper.executeInUI(() -> Messages.showWarningDialog("No components to link to", "Link component"));
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Link component"));
      }
    });
  }

  private List<Component> getTargetComponents(Odo odo, OpenShiftClient client, String project, String application) {
    return odo.getComponents(client, project, application).stream().filter(component -> component.getState() == ComponentState.PUSHED).collect(Collectors.toList());
  }
}
