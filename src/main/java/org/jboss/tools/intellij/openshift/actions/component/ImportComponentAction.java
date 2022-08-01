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
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentModel;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ImportComponentAction extends CreateComponentAction {
    public ImportComponentAction() {
        super(ComponentNode.class);
    }

    @Override
    protected String getTelemetryActionName() { return "import component"; }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            visible = ((ComponentNode) selected).getComponent().getState() == ComponentState.NO_CONTEXT;
        }
        return visible;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = componentNode.getComponent();
        NamespaceNode namespaceNode = componentNode.getParent();
        ApplicationsTreeStructure structure = (ApplicationsTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY);
        CompletableFuture.runAsync(() -> {
            try {
                ApplicationsRootNode root = componentNode.getRoot();
                Project project = root.getProject();
                ComponentInfo info = odo.getComponentInfo(namespaceNode.getName(),
                        component.getName(), null, component.getInfo().getComponentKind());
                CreateComponentModel model = getModel(project, odo, component.getName(), info);
                process((ParentableNode) selected, odo, namespaceNode.getName(), root, model, structure);

            } catch (IOException e) {
                sendTelemetryError(e);
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Import"));
            }
        });
    }

    @NotNull
    private CreateComponentModel getModel(Project project, Odo odo, String name, ComponentInfo info) throws IOException {
        List<DevfileComponentType> types = odo.getComponentTypes();
        CreateComponentModel model = new CreateComponentModel("Import component", project, odo, types);
        ComponentType type = select(types, info.getComponentTypeName());
        model.setName(name);
        model.setSelectedComponentType(type);
        model.setImportMode(true);
        return model;
    }

    private ComponentType select(List<DevfileComponentType> types, String componentTypeName) {
        return types.stream().filter(type -> componentTypeName.equals(type.getName())).
                findFirst().orElse(null);
    }
}
