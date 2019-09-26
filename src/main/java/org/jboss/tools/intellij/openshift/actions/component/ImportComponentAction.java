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
import com.intellij.openapi.project.Project;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentModel;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
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
      visible = ((Component)((ComponentNode)selected).getUserObject()).getState() == ComponentState.NO_CONTEXT;
    }
    return visible;
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = (Component) componentNode.getUserObject();
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        ApplicationsRootNode root = (ApplicationsRootNode)((LazyMutableTreeNode)selected).getRoot();
        ApplicationTreeModel rootModel = root.getModel();
        Project project = rootModel.getProject();
        ComponentInfo info = odo.getComponentInfo(root.getClient(), projectNode.toString(), applicationNode.toString(), component.getName());
        CreateComponentModel model = getModel(project, odo.getComponentTypes(), applicationNode.toString(), component.getName(), info);
        process((LazyMutableTreeNode) selected, odo, projectNode.toString(), Optional.of(applicationNode.toString()), rootModel, model);

      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Import", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  @NotNull
  private CreateComponentModel getModel(Project project, List<ComponentType> types, String application, String name, ComponentInfo info) {
    CreateComponentModel model =  new CreateComponentModel("Import component");
    model.setProject(project);
    model.setApplication(application);
    model.setName(name);
    model.setComponentTypes(types.toArray(new ComponentType[types.size()]));
    model.setSourceType(info.getSourceType());
    model.setComponentTypeName(info.getComponentTypeName());
    model.setComponentTypeVersion(info.getComponentTypeVersion());
    model.setGitURL(info.getRepositoryURL());
    model.setGitReference(info.getRepositoryReference());
    model.setBinaryFilePath(info.getBinaryURL()); //TODO
    model.setImportMode(true);
    return model;

  }
}
