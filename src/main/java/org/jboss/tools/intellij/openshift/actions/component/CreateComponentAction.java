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
import com.intellij.openapi.ui.Messages;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationTreeModel;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentModel;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentSourceType;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CreateComponentAction extends OdoAction {
  public CreateComponentAction() {
    super(ApplicationNode.class, ProjectNode.class);
  }

  protected CreateComponentAction(Class... clazz) {
    super(clazz);
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
        CreateComponentModel model = getModel(project, application, odo.getComponentTypes(), p -> rootModel.getComponents().containsKey(p));
        process((LazyMutableTreeNode) selected, odo, projectName, application, rootModel, model);
      } catch (IOException e) {
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create component"));
      }
    });
  }

  protected void process(LazyMutableTreeNode selected, Odo odo, String projectName, Optional<String> application, ApplicationTreeModel rootModel, CreateComponentModel model) throws IOException {
    boolean doit = UIHelper.executeInUI(() -> showDialog(model));
    if (doit) {
      createComponent(odo, projectName, application.orElse(model.getApplication()), model);
      rootModel.addContext(model.getContext());
      selected.reload();
    }
  }

  private void createComponent(Odo odo, String project, String application, CreateComponentModel model) throws IOException{
    if (model.getSourceType() == ComponentSourceType.LOCAL) {
      odo.createComponentLocal(project, application, model.getComponentTypeName(), model.getComponentTypeVersion(), model.getName(), model.getContext(), model.isPushAfterCreate());
    } else if (model.getSourceType() == ComponentSourceType.GIT) {
      odo.createComponentGit(project, application, model.getContext(), model.getComponentTypeName(), model.getComponentTypeVersion(), model.getName(), model.getGitURL(), model.getGitReference(), model.isPushAfterCreate());
    } else if (model.getSourceType() == ComponentSourceType.BINARY) {
      Path binary = Paths.get(model.getBinaryFilePath());
      if (binary.isAbsolute()) {
       binary = Paths.get(model.getContext()).relativize(binary);
      }
      odo.createComponentBinary(project, application, model.getContext(), model.getComponentTypeName(), model.getComponentTypeVersion(), model.getName(), binary.toString(), model.isPushAfterCreate());
    }
  }

  protected boolean showDialog(CreateComponentModel model) {
    CreateComponentDialog dialog = new CreateComponentDialog(model.getProject(), true, model);
    dialog.show();
    return dialog.isOK();
  }

  @NotNull
  protected CreateComponentModel getModel(Project project, Optional<String> application, List<ComponentType> types, Predicate<String> componentChecker) {
    CreateComponentModel model =  new CreateComponentModel("Create component");
    model.setProject(project);
    model.setComponentTypesTree(types);
    if (application.isPresent()) {
      model.setApplication(application.get());
    }
    model.setComponentPredicate(componentChecker);
    return model;
  }

}
