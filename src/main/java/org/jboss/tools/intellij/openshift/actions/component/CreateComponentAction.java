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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.apache.commons.lang3.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.ui.component.CreateComponentModel;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Predicate;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.clearProcessing;
import static org.jboss.tools.intellij.openshift.actions.NodeUtils.setProcessing;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class CreateComponentAction extends OdoAction {
  public CreateComponentAction() {
    super(NamespaceNode.class);
  }

  protected CreateComponentAction(Class... clazz) {
    super(clazz);
  }

  public static void execute(ParentableNode<?> parentNode) {
    CreateComponentAction action = (CreateComponentAction) ActionManager.getInstance().getAction(CreateComponentAction.class.getName());
    NamespaceNode namespaceNode = (NamespaceNode) parentNode;
    action.telemetrySender = new TelemetrySender(PREFIX_ACTION + action.getTelemetryActionName());
    Odo odo = namespaceNode.getRoot().getOdo().getNow(null);
    if (odo == null) {
      return;
    }
    action.doActionPerformed(namespaceNode, odo, namespaceNode.getName(),
              namespaceNode.getRoot(), namespaceNode.getRoot().getProject());
  }

  @Override
  protected String getTelemetryActionName() { return "create component"; }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
    NamespaceNode namespaceNode = ((NamespaceNode) selected);
    ApplicationsRootNode rootNode = ((ParentableNode<Object>) selected).getRoot();
    Project project = rootNode.getProject();
    doActionPerformed((NamespaceNode) selected, odo, namespaceNode.getName(), rootNode, project);
  }

  private void doActionPerformed(NamespaceNode namespaceNode, Odo odo, String projectName, ApplicationsRootNode rootNode, Project project) {
    runWithProgress((ProgressIndicator progress) -> {
        try {
          CreateComponentModel model = getModel(project, odo, p -> rootNode.getComponents().containsKey(p));
          boolean create = !UIHelper.executeInUI(() -> showDialog(model));
          if (create) {
            sendTelemetryResults(TelemetryResult.ABORTED);
            return;
          }
          setProcessing("Creating component...", namespaceNode);
          createComponent(odo, projectName, model);
          clearProcessing(namespaceNode);
          rootNode.addContext(model.getContext());
          NodeUtils.fireModified(namespaceNode);
          sendTelemetryResults(TelemetryResult.SUCCESS);
        } catch (IOException e) {
          clearProcessing(namespaceNode);
          sendTelemetryError(e);
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create Component"));
        }
      },
      "Create Component...",
      project);
  }

  protected void createComponent(Odo odo, String projectName, CreateComponentModel model)
    throws IOException {
    computeTelemetry(model);
    String type = model.getSelectedComponentType() != null ?
      model.getSelectedComponentType().getName() : null;
    String registry = model.getSelectedComponentType() instanceof DevfileComponentType ?
      ((DevfileComponentType) model.getSelectedComponentType()).getDevfileRegistry().getName() : null;
    String devFile = model.isProjectHasDevfile() ?
      Constants.DEVFILE_NAME : null;
    odo.createComponent(projectName,
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
  protected CreateComponentModel getModel(Project project, Odo odo, Predicate<String> componentChecker) throws IOException{
    CreateComponentModel model =  new CreateComponentModel("Create component", project, odo, odo.getComponentTypes());
    model.setComponentPredicate(componentChecker);
    return model;
  }

  private void computeTelemetry(CreateComponentModel model) {
    telemetrySender
        .addProperty(TelemetryService.PROP_COMPONENT_HAS_LOCAL_DEVFILE, String.valueOf(model.isProjectHasDevfile()));
    telemetrySender
        .addProperty(TelemetryService.PROP_COMPONENT_PUSH_AFTER_CREATE, String.valueOf(model.isDevModeAfterCreate()));
    if (!model.isProjectHasDevfile() && StringUtils.isNotBlank(model.getSelectedComponentType().getName())) {
      telemetrySender.addProperty(TelemetryService.PROP_COMPONENT_KIND, "devfile:" + model.getSelectedComponentType().getName());
    }
    if (StringUtils.isNotBlank(model.getSelectedComponentStarter())) {
      telemetrySender.addProperty(TelemetryService.PROP_COMPONENT_SELECTED_STARTER, model.getSelectedComponentStarter());
    }
  }
}