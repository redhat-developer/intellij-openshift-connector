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
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class DescribeComponentAction extends OdoAction {
  public DescribeComponentAction() {
    super(ComponentNode.class);
  }

  @Override
  protected String getTelemetryActionName() { return "describe component"; }

  @Override
  public boolean isVisible(Object selected) {
    boolean visible = super.isVisible(selected);
    return visible;
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = componentNode.getComponent();
    NamespaceNode namespaceNode = componentNode.getParent();
    runWithProgress((ProgressIndicator progress) -> {
          try {
            odo.describeComponent(namespaceNode.getName(), component.getPath(), component.getName());
            sendTelemetryResults(TelemetryResult.SUCCESS);
          } catch (IOException e) {
            sendTelemetryError(e);
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Describe"));
          }
        },
        "Describing Component " + component.getName() + "...",
      getEventProject(anActionEvent));
  }
}
