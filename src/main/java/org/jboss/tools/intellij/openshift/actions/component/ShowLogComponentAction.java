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
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeature;
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
                visible = isDevOrDebugAndLogNotRunning(componentNode) || isDeployAndLogNotRunning(componentNode);
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

    protected void doLog(ComponentNode componentNode, Odo odo, boolean follow) {
        try {
            Component component = componentNode.getComponent();
            NamespaceNode namespaceNode = componentNode.getParent();
            Optional<Boolean> isDeploy = isRunningInBothDevAndDeploy(componentNode);
            if (isDeploy.isEmpty()) {
                int choice = Messages.showDialog(componentNode.getRoot().getProject(), "Component is running in both dev and deploy mode, which container do you want to get logs from ?", getActionName(), new String[]{"Dev", "Deploy"}, 0, null);
                if (choice == 0) {
                    isDeploy = Optional.of(Boolean.FALSE);
                } else if (choice == 1) {
                    isDeploy = Optional.of(Boolean.TRUE);
                }
            }
            if (isDeploy.isPresent()) {
                boolean deploy = isDeploy.get();
                CompletableFuture.runAsync(() -> {
                    String platform = getPlatform(component);
                    try {
                        if (follow) {
                            odo.follow(namespaceNode.getName(), component.getPath(), component.getName(), deploy, platform);
                        } else {
                            odo.log(namespaceNode.getName(), component.getPath(), component.getName(), deploy, platform);
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

    private Optional<Boolean> isRunningInBothDevAndDeploy(ComponentNode componentNode) throws IOException {
        Component component = componentNode.getComponent();
        if (isDevOrDebugAndLogNotRunning(componentNode) && !isDeploy(component)) {
            return Optional.of(Boolean.FALSE);
        }
        if (!isDev(component) && !isDebug(component)
                && isDeployAndLogNotRunning(componentNode)) {
            return Optional.of(Boolean.TRUE);
        }
        if ((isDev(component) || isDebug(component)) && isDeploy(component)) {
            if (isLogRunningForDevOrDebug(componentNode)) {
                return Optional.of(Boolean.TRUE);
            } else if (isLogRunningForDeploy(componentNode)) {
                return Optional.of(Boolean.FALSE);
            }
        }
        return Optional.empty();
    }

    private static boolean isDeploy(Component component) {
        return component.getLiveFeatures().isDeploy();
    }

    private  boolean isDev(Component component) {
        return component.getLiveFeatures().isDev();
    }

    private  boolean isDebug(Component component) {
        return component.getLiveFeatures().isDebug();
    }

    private  boolean isLogRunningForDevOrDebug(ComponentNode componentNode) throws IOException {
        return componentNode.getRoot().getOdo().isLogRunning(componentNode.getComponent().getPath(), componentNode.getComponent().getName(), false);
    }

    private  boolean isLogRunningForDeploy(ComponentNode componentNode) throws IOException {
        return componentNode.getRoot().getOdo().isLogRunning(componentNode.getComponent().getPath(), componentNode.getComponent().getName(), true);
    }

    private boolean isDevOrDebugAndLogNotRunning(ComponentNode componentNode) throws IOException {
        return (isDev(componentNode.getComponent()) || isDebug(componentNode.getComponent()))
                && !isLogRunningForDevOrDebug(componentNode);
    }

    private boolean isDeployAndLogNotRunning(ComponentNode componentNode) throws IOException {
        return (isDeploy(componentNode.getComponent())
                && !isLogRunningForDeploy(componentNode));
    }

    private String getPlatform(Component component) {
        if (component.getLiveFeatures().is(ComponentFeature.DEV_ON_PODMAN)) {
            return ComponentFeature.Constants.PODMAN;
        }
        return null;
    }
}
