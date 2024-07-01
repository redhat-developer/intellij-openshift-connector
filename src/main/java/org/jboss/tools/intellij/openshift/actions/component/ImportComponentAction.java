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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentModel;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;

public class ImportComponentAction extends CreateComponentAction {
  public ImportComponentAction() {
    super(ComponentNode.class);
  }

  @Override
  public String getTelemetryActionName() {return "import component";}

  @Override
  public boolean isVisible(Object selected) {
    return super.isVisible(selected)
      && NodeUtils.hasContext(selected);
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = componentNode.getComponent();
    NamespaceNode namespaceNode = componentNode.getParent();
    Project project = getEventProject(anActionEvent);
    runWithProgress((ProgressIndicator progress) -> {
        try {
          ComponentInfo info = odo.getComponentInfo(
            namespaceNode.getName(),
            component.getName(),
            null,
            component.getInfo().getComponentKind());
          CreateComponentModel model = getModel(project, odo, component.getName(), info);
          setProcessing("Importing component " + component.getName() + "...", namespaceNode);
          createComponent(odo, model);
          clearProcessing(namespaceNode);
        } catch (IOException e) {
          clearProcessing(namespaceNode);
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Import"));
        }
      },
      "Importing Component...",
      project);
  }

  @NotNull
  private CreateComponentModel getModel(Project project, Odo odo, String name, ComponentInfo info) throws IOException {
    List<DevfileComponentType> types = odo.getAllComponentTypes();
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
