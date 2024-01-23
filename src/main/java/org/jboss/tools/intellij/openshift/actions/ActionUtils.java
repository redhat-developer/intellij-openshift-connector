/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ActionUtils {

  private ActionUtils() {
  }

  /**
   * Returns the {@link ApplicationsTreeStructure} for a given {@link AnActionEvent}.
   * Throws if it fails to it.
   *
   * @param e AnActionEvent to retrieve the ApplicationTreeStructure from
   * @return the ApplicationTreeStructure
   */
  public static ApplicationsTreeStructure getApplicationTreeStructure(AnActionEvent e) {
    Object structure = getTree(e).getClientProperty(Constants.STRUCTURE_PROPERTY);
    if (!(structure instanceof ApplicationsTreeStructure)) {
      throw new IllegalArgumentException("invalid context: org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure");
    }
    return (ApplicationsTreeStructure) structure;
  }

  private static JTree getTree(AnActionEvent e) {
    Component component = e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
    if (!(component instanceof Tree)) {
      throw new IllegalArgumentException("invalid context: not a com.intellij.ui.treeStructure.Tree");
    }
    return (Tree) component;
  }

  public static ApplicationsRootNode getApplicationRootNode(AnActionEvent e) {
    return (ApplicationsRootNode) getApplicationTreeStructure(e).getApplicationsRoot();
  }

  public static void runWithProgress(Consumer<ProgressIndicator> consumer, String message, Project project) {
    ProgressManager.getInstance().run(
      new Task.Backgroundable(project, message, true) {
        @Override
        public void run(ProgressIndicator progress) {
          consumer.accept(progress);
        }
      });
  }

  public static Point getLocation(AnActionEvent actionEvent) {
    if (actionEvent == null) {
      return null;
    }
    MouseEvent event = ((MouseEvent) actionEvent.getInputEvent());
    if (event == null) {
      return null;
    }
    return event.getLocationOnScreen();
  }

}
