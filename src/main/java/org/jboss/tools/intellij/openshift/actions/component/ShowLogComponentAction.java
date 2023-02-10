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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class ShowLogComponentAction extends ContextAwareComponentAction {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShowLogComponentAction.class);

  @Override
  protected String getTelemetryActionName() { return "show component log"; }

  protected String getActionName() {
    return "Show log";
  }

  @Override
  public boolean isVisible(Object selected) {
    boolean visible = super.isVisible(selected);
    try {
      if (visible) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = ((ComponentNode) selected).getComponent();
        visible &= ((component.getLiveFeatures().isDev() || component.getLiveFeatures().isDebug()) &&
                !componentNode.getRoot().getOdo().isLogRunning(component.getPath(), component.getName(), false)) ||
                (component.getLiveFeatures().isDeploy() &&
                        !componentNode.getRoot().getOdo().isLogRunning(component.getPath(), component.getName(), true));
      }
    } catch (IOException e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
    }
    return visible;
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
    doLog((ComponentNode) selected, odo, false);
  }

  protected void doLog(ComponentNode selected, Odo odo, boolean follow) {
    try {
      ComponentNode componentNode = selected;
      Component component = componentNode.getComponent();
      NamespaceNode namespaceNode = componentNode.getParent();
      Optional<Boolean> deploy = isDeploy(odo, selected);
      if (deploy.isEmpty()) {
        int choice = Messages.showDialog(componentNode.getRoot().getProject(), "Component is running in both dev and deploy mode, which container do you want to get logs from ?", getActionName(),new String[] {"Dev", "Deploy"}, 0, null);
        if (choice == 0) {
          deploy = Optional.of(Boolean.FALSE);
        } else if (choice == 1) {
          deploy = Optional.of(Boolean.TRUE);
        }
      }
      if (deploy.isPresent()) {
        Optional<Boolean> finalDeploy = deploy;
        CompletableFuture.runAsync(() -> {
          try {
            if (follow) {
              odo.follow(namespaceNode.getName(), component.getPath(), component.getName(), finalDeploy.get());
            } else {
              odo.log(namespaceNode.getName(), component.getPath(), component.getName(), finalDeploy.get());
            }
            sendTelemetryResults(TelemetryResult.SUCCESS);
          } catch (IOException e) {
            sendTelemetryError(e);
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), getActionName()));
          }
        });
      }
    } catch (IOException e) {
      Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), getActionName());
    }
  }

  private Optional<Boolean> isDeploy(Odo odo, ComponentNode componentNode) throws IOException {
    Optional<Boolean> result = Optional.empty();
    Component component = componentNode.getComponent();
    if ((component.getLiveFeatures().isDev() || component.getLiveFeatures().isDebug()) &&
            !component.getLiveFeatures().isDeploy() &&
            !odo.isLogRunning(component.getPath(), component.getName(), false)) {
      result = Optional.of(Boolean.FALSE);
    }
    if (!component.getLiveFeatures().isDev() && !component.getLiveFeatures().isDebug() &&
            component.getLiveFeatures().isDeploy() &&
            !odo.isLogRunning(component.getPath(), component.getName(), true)) {
      result = Optional.of(Boolean.TRUE);
    }
    if ((component.getLiveFeatures().isDev() || component.getLiveFeatures().isDebug()) &&
            component.getLiveFeatures().isDeploy()) {
      if (odo.isLogRunning(component.getPath(), component.getName(), false)) {
        result = Optional.of(Boolean.TRUE);
      } else if (odo.isLogRunning(component.getPath(), component.getName(), true)) {
        result = Optional.of(Boolean.FALSE);
      }
    }
    return result;
  }
}
