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

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.*;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.net.ServerSocket;

public class DebugComponentAction extends OdoAction {
    public DebugComponentAction() {
        super(ComponentNode.class);
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            ComponentNode componentNode = (ComponentNode) selected;
            Component component = (Component) componentNode.getUserObject();
            return (isPushed(component) && isDebuggable(componentNode, component));
        }
        return false;
    }

    private boolean isDebuggable(ComponentNode componentNode, Component component) {
        ApplicationNode applicationNode = (ApplicationNode) componentNode.getParent();
        try {
            Odo odo = ((ApplicationsRootNode) componentNode.getRoot()).getOdo();
            LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
            ApplicationsRootNode root = (ApplicationsRootNode) applicationNode.getRoot();
            ComponentInfo info = odo.getComponentInfo(root.getClient(), projectNode.toString(), applicationNode.toString(), component.getName());
            //TODO check here is the port is in use, avoiding the error message below when execute the action.
            return info.getComponentTypeName().equals("java") || info.getComponentTypeName().equals("nodejs");
        } catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Debug"));
        }
        return false;
    }

    private boolean isPushed(Component component) {
        return component.getState() == ComponentState.PUSHED;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = (Component) componentNode.getUserObject();
        ApplicationNode applicationNode = (ApplicationNode) componentNode.getParent();
        LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
        //TODO check if local port is modified
        Integer port = new Integer("5858");
        if (!isAvailable(port)) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error, cannot run Odo Debug using port " + port + ".\nPort is already in use.", "Odo Debug"));
            return;
        }
        Project project = anActionEvent.getData(CommonDataKeys.PROJECT);

        RunnerAndConfigurationSettings runSettings = RunManager.getInstance(project).createConfiguration(
                component.getName() + " Remote Debug with odo", RemoteConfigurationType.getInstance().getFactory());

        RemoteConfiguration remoteConfiguration = (RemoteConfiguration) runSettings.getConfiguration();
        remoteConfiguration.HOST = "localhost";
        remoteConfiguration.PORT = port.toString();

        ExecHelper.submit(() -> {
            try {
                odo.debug(projectNode.toString(), applicationNode.toString(), component.getPath(), component.getName(), port);
                try {
                    Thread.sleep(7000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    ProgramRunnerUtil.executeConfiguration(runSettings, DefaultDebugExecutor.getDebugExecutorInstance());
                });

            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Odo Debug"));
            }
        });

    }

    private boolean isAvailable(Integer port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
