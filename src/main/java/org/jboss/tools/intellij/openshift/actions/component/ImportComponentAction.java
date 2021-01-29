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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentModel;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentKind;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ImportComponentAction extends CreateComponentAction {
    public ImportComponentAction() {
        super(ComponentNode.class);
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            visible = ((Component) ((ComponentNode) selected).getComponent()).getState() == ComponentState.NO_CONTEXT;
        }
        return visible;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = (Component) componentNode.getComponent();
        ApplicationNode applicationNode = componentNode.getParent();
        NamespaceNode namespaceNode = applicationNode.getParent();
        CompletableFuture.runAsync(() -> {
            try {
                ApplicationsRootNode root = componentNode.getRoot();
                Project project = root.getProject();
                ComponentInfo info = odo.getComponentInfo(namespaceNode.getName(), applicationNode.getName(), component.getName(), component.getInfo().getComponentKind());
                CreateComponentModel model = getModel(project, odo.getComponentTypes(), applicationNode.getName(), component.getName(), info, component.getInfo().getComponentKind());
                process((ParentableNode) selected, odo, namespaceNode.getName(), Optional.of(applicationNode.getName()), root, model, anActionEvent);

            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Import"));
            }
        });
    }

    @NotNull
    private CreateComponentModel getModel(Project project, List<ComponentType> types, String application, String name, ComponentInfo info, ComponentKind kind) {
        CreateComponentModel model = new CreateComponentModel("Import component");
        model.setProject(project);
        model.setApplication(application);
        model.setName(name);
        model.setComponentTypesTree(types);
        model.setSourceType(info.getSourceType());
        model.setComponentTypeName(info.getComponentTypeName());
        model.setComponentTypeVersion(info.getComponentTypeVersion());
        model.setGitURL(info.getRepositoryURL());
        model.setGitReference(info.getRepositoryReference());
        model.setBinaryFilePath(info.getBinaryURL());
        model.setComponentKind(kind);
        model.setImportMode(true);
        return model;
    }
}
