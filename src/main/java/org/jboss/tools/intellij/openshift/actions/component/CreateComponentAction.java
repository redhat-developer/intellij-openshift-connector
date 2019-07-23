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
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentModel;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CreateComponentAction extends OdoAction {
  public CreateComponentAction() {
    super(ApplicationNode.class, ProjectNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    final Optional<String> application;
    String projectName;
    if (selected instanceof ApplicationNode) {
      application = Optional.of(selected.toString());
      projectName =  ((LazyMutableTreeNode)selected).getParent().toString();
    } else {
      application = Optional.empty();
      projectName = selected.toString();
    }
    ApplicationTreeModel rootModel = ((ApplicationsRootNode)((LazyMutableTreeNode)selected).getRoot()).getModel();
    Project project = rootModel.getProject();
    CompletableFuture.runAsync(() -> {
      try {
        CreateComponentModel model = UIHelper.executeInUI(() -> {
          try {
            return showDialog(project, application, odo.getComponentTypes());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
        if (model != null) {
          createComponent(odo, projectName, application.orElse(model.getApplication()), model);
          rootModel.addContext(model.getContext());
          ((LazyMutableTreeNode)selected).reload();
          if (model.isPushAfterCreate()) {
              odo.push(projectName, application.orElse(model.getApplication()), model.getContext(), model.getName());
          }
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create component", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  private void createComponent(Odo odo, String project, String application, CreateComponentModel model) throws IOException{
    if (model.getSourceType() == CreateComponentModel.SourceType.LOCAL) {
      odo.createComponentLocal(project, application, model.getComponentTypeName(), model.getComponentTypeVersion(), model.getName(), model.getContext());
    } else {
      odo.createComponentGit(project, application, model.getContext(), model.getComponentTypeName(), model.getComponentTypeVersion(), model.getName(), model.getGitURL());
    }
  }

  protected CreateComponentModel showDialog(Project project, Optional<String> application, List<ComponentType> types) {
    CreateComponentModel model =  new CreateComponentModel("Create component");
    model.setProject(project);
    model.setComponentTypes(types.toArray(new ComponentType[types.size()]));
    if (application.isPresent()) {
      model.setApplication(application.get());
    }
    CreateComponentDialog dialog1 = new CreateComponentDialog(project, true, model);
    dialog1.show();
    return dialog1.isOK()?model:null;
  }

}
