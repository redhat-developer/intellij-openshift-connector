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
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LinkComponentAction extends OdoAction {

  public LinkComponentAction() {
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

  protected String getSelectedTargetComponent(Odo odo, OpenShiftClient client, String project, String application, String component) {
    String targetComponent = null;

    List<Component> components = odo.getComponents(client, project, application)
            .stream().filter(comp -> !comp.getName().equals(component)).collect(Collectors.toList());
    if (!components.isEmpty()) {
      if (components.size() == 1) {
        targetComponent = components.get(0).getName();
      } else {
        Object[] componentsArray = components.stream().map(Component::getName).toArray();
        targetComponent = (String) UIHelper.executeInUI(() -> JOptionPane.showInputDialog(null, "Select component", "Link component", JOptionPane.QUESTION_MESSAGE, null, componentsArray, componentsArray[0]));
      }
    }
    return targetComponent;
  }

  protected Integer getSelectedPort(Odo odo, OpenShiftClient client, String project, String application, String component) {
    Integer port = null;

    List<Integer> ports = odo.getServicePorts(client, project, application, component);
    if (!ports.isEmpty()) {
      if (ports.size() == 1) {
        port = ports.get(0);
      } else {
        Object[] portsArray = ports.toArray();
        port = (Integer) UIHelper.executeInUI(() -> JOptionPane.showInputDialog(null, "Select port", "Link component", JOptionPane.QUESTION_MESSAGE, null, portsArray, portsArray[0]));
      }
    }
    return port;
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component sourceComponent = (Component) componentNode.getUserObject();
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    OpenShiftClient client = ((ApplicationsRootNode)componentNode.getRoot()).getClient();
    CompletableFuture.runAsync(() -> {
      try {
        String targetComponent = getSelectedTargetComponent(odo, client, projectNode.toString(), applicationNode.toString(), sourceComponent.getName());
          if (targetComponent != null) {
            Integer port = getSelectedPort(odo, client, projectNode.toString(), applicationNode.toString(), targetComponent);
            if (port != null) {
              odo.link(projectNode.toString(), applicationNode.toString(), sourceComponent.getName(), sourceComponent.getPath(), targetComponent, port.intValue());
              Notifications.Bus.notify(new Notification(Constants.GROUP_DISPLAY_ID, "Link component", "Component linked to " + targetComponent,
                      NotificationType.INFORMATION));
            } else {
              UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "No ports to link to", "Link component", JOptionPane.WARNING_MESSAGE));
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
