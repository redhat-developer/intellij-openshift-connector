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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.ActionUtils;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentModel;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Predicate;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateComponentAction extends OdoAction {
  public CreateComponentAction() {
    super(NamespaceNode.class);
  }

  protected CreateComponentAction(Class... clazz) {
    super(clazz);
  }

  public static void execute(ParentableNode<?> node) {
    if (node == null) {
      return;
    }
    Odo odo = node.getRoot().getOdo().getNow(null);
    if (odo == null) {
      return;
    }
    NamespaceNode namespaceNode = (NamespaceNode) node;
    CreateComponentAction action = ActionUtils.createAction(CreateComponentAction.class.getName());
    action.doActionPerformed(
      namespaceNode,
      odo,
      namespaceNode.getRoot(),
      namespaceNode.getRoot().getProject());
  }

  @Override
  public String getTelemetryActionName() {
    return "create component";
  }

  @Override
  public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo) {
    if (selected == null) {
      return;
    }
    NamespaceNode namespaceNode = (NamespaceNode) selected;
    ApplicationsRootNode rootNode = namespaceNode.getRoot();
    Project project = rootNode.getProject();
    doActionPerformed((NamespaceNode) selected, odo, rootNode, project);
  }

  private void doActionPerformed(NamespaceNode namespaceNode, Odo odo, ApplicationsRootNode rootNode, Project project) {
    runWithProgress(
      (ProgressIndicator progress) -> {
        try {
          CreateComponentModel model = getModel(project, odo, p -> rootNode.getLocalComponents().containsKey(p));
          boolean create = !UIHelper.executeInUI(() -> showDialog(model));
          if (create) {
            sendTelemetryResults(TelemetryResult.ABORTED);
            return;
          }
          setProcessing("Creating component...", namespaceNode);
          createComponent(odo, model);
          clearProcessing(namespaceNode);
          rootNode.addContext(model.getContext());
          NodeUtils.fireModified(namespaceNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
        } catch (IOException e) {
          clearProcessing(namespaceNode);
          sendTelemetryError(e.getMessage());
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create Component"));
        }
      },
      "Create Component...",
      project);
  }

  protected void createComponent(Odo odo, CreateComponentModel model)
    throws IOException {
    computeTelemetry(model);
    String type = model.getSelectedComponentType() != null ?
      model.getSelectedComponentType().getName() : null;
    String registry = model.getSelectedComponentType() instanceof DevfileComponentType devfileComponentType ?
      devfileComponentType.getDevfileRegistry().getName() : null;
    String devFile = model.isProjectHasDevfile() ?
      Constants.DEVFILE_NAME : null;
    odo.createComponent(
      type,
      registry,
      model.getName(),
      model.getContext(),
      devFile,
      model.getSelectedComponentStarter());
  }

  protected boolean showDialog(CreateComponentModel model) {
    CreateComponentDialog dialog = new CreateComponentDialog(model.getProject(), true, model);
    dialog.show();
    return dialog.isOK();
  }

  @NotNull
  protected CreateComponentModel getModel(Project project, Odo odo, Predicate<String> componentChecker) throws IOException {
    CreateComponentModel model = new CreateComponentModel("Create component", project, odo, odo.getAllComponentTypes());
    model.setComponentPredicate(componentChecker);
    return model;
  }

  private void computeTelemetry(CreateComponentModel model) {
    addProperty(TelemetryService.PROP_COMPONENT_HAS_LOCAL_DEVFILE, String.valueOf(model.isProjectHasDevfile()));
    addProperty(TelemetryService.PROP_COMPONENT_PUSH_AFTER_CREATE, String.valueOf(model.isDevModeAfterCreate()));
    if (!model.isProjectHasDevfile() && !StringUtil.isEmptyOrSpaces(model.getSelectedComponentType().getName())) {
      addProperty(TelemetryService.PROP_COMPONENT_KIND, "devfile:" + model.getSelectedComponentType().getName());
    }
    if (!StringUtil.isEmptyOrSpaces(model.getSelectedComponentStarter())) {
      addProperty(TelemetryService.PROP_COMPONENT_SELECTED_STARTER, model.getSelectedComponentStarter());
    }
  }
}