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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.BaseNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.ui.binding.BindingDetailDialog;
import org.jboss.tools.intellij.openshift.utils.odo.Binding;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.Service;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.jboss.tools.intellij.openshift.Constants.GROUP_DISPLAY_ID;
import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class LinkServiceAction extends OdoAction {
    public LinkServiceAction() {
        super(ComponentNode.class);
    }

    @Override
    protected String getTelemetryActionName() {
        return "link component to service";
    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = componentNode.getComponent();
        NamespaceNode namespaceNode = componentNode.getParent();
        runWithProgress((ProgressIndicator progress) -> {
            try {
                List<Service> services = odo.getServices(namespaceNode.getName());
                if (!services.isEmpty()) {
                    String serviceName = getServiceName(services);
                    if (!Objects.isNull(serviceName)) {
                        Service service = services.stream()
                          .filter(s -> serviceName.equals(s.getName()))
                          .findFirst()
                          .orElseThrow();
                        setProcessing("Linking Service...", componentNode);
                        linkService(service, component, namespaceNode, odo, anActionEvent.getProject());
                        clearProcessing(namespaceNode);
                    }
                } else {
                    String message = "No services to link to";
                    sendTelemetryError(message);
                    UIHelper.executeInUI(() -> Messages.showWarningDialog(message, "Link service"));
                }
            } catch (IOException | NoSuchElementException e) {
                clearProcessing(namespaceNode);
                sendTelemetryError(e);
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Link service"));
            }
        },
        "Link Service...",
        getEventProject(anActionEvent));
    }

    private static String getServiceName(List<Service> services) {
        String[] servicesArray = services.stream()
          .map(Service::getName)
          .toArray(String[]::new);
        return UIHelper.executeInUI(() -> Messages.showEditableChooseDialog("Link service",
          "Select service",
          Messages.getQuestionIcon(),
          servicesArray,
          servicesArray[0],
          null));
    }

    private void linkService(Service service, Component component, BaseNode<?> namespaceNode, Odo odo, Project project) throws IOException {
        Notification notification = notify("Linking component to service " + service.getName());
        String target = service.getName() + '/' + service.getKind() + "." + service.getApiVersion();
        Binding binding = odo.link(namespaceNode.getName(), component.getPath(), component.getName(), target);
        notification.expire();
        notify("Component linked to " + service.getName());
        NodeUtils.fireModified(namespaceNode);
        sendTelemetryResults(TelemetryResult.SUCCESS);
        if (!binding.getEnvironmentVariables().isEmpty()) {
            ApplicationManager.getApplication().invokeLater(() -> {
                BindingDetailDialog dialog = new BindingDetailDialog(project, null, binding);
                dialog.show();
            });
        }
    }

    @NotNull
    private static Notification notify(String content) {
        Notification notification = new Notification(GROUP_DISPLAY_ID, "Link service", content, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
        return notification;
    }

    @Override
    public boolean isVisible(Object selected) {
        return super.isVisible(selected)
                && NodeUtils.hasContext(selected);
    }
}
