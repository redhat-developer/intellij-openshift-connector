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

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.intellij.openapi.ui.Messages.CANCEL_BUTTON;
import static com.intellij.openapi.ui.Messages.getWarningIcon;
import static org.jboss.tools.intellij.openshift.Constants.COMPONENT_MIGRATION_MESSAGE;
import static org.jboss.tools.intellij.openshift.Constants.COMPONENT_MIGRATION_TITLE;
import static org.jboss.tools.intellij.openshift.Constants.HELP_LABEL;
import static org.jboss.tools.intellij.openshift.Constants.UNDEPLOY_LABEL;

public class PushComponentAction extends ContextAwareComponentAction {
  protected String getActionName() {
    return "Push";
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component component = (Component) ((ComponentNode) selected).getUserObject();
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    OpenShiftClient client = ((ApplicationsRootNode) componentNode.getRoot()).getClient();
    CompletableFuture.runAsync(() -> {
      try {
        if (checkMigrated(odo, client, projectNode.toString(), applicationNode.toString(), component)) {
          process(odo, projectNode.toString(), applicationNode.toString(), component);
          component.setState(ComponentState.PUSHED);
          componentNode.reload();
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), getActionName()));
      }
    });
  }

  protected void process(Odo odo, String project, String application, Component component) throws IOException {
    odo.push(project, application, component.getPath(), component.getName());
  }

  private boolean checkMigrated(Odo odo, OpenShiftClient client, String project, String application, Component component) throws IOException {
    boolean ok = true;
    if (component.getState() == ComponentState.PUSHED) {
      ComponentInfo info = odo.getComponentInfo(client, project, application, component.getName());
      if (info.isMigrated()) {
        int choice = UIHelper.executeInUI(() -> Messages.showDialog(COMPONENT_MIGRATION_MESSAGE, COMPONENT_MIGRATION_TITLE, new String[]{UNDEPLOY_LABEL, HELP_LABEL, CANCEL_BUTTON}, 0, getWarningIcon()));
        if (choice == 0) {
          odo.undeployComponent(project, application, component.getPath(), component.getName());
        } else if (choice == 1) {
          BrowserUtil.browse(Constants.MIGRATION_HELP_PAGE_URL);
          ok = false;
        } else {
          ok = false;
        }
      }
    }
    return ok;
  }
}
