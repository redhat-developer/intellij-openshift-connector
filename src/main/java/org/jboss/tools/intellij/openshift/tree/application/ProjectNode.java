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
package org.jboss.tools.intellij.openshift.tree.application;

import io.fabric8.openshift.api.model.Project;
import org.jboss.tools.intellij.openshift.tree.KubernetesResourceMutableTreeNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;

public class ProjectNode extends KubernetesResourceMutableTreeNode {
  public ProjectNode(Project project) {
    super(project);
  }

  @Override
  public void load() {
    super.load();
    try {
      Odo.get().getApplications(toString()).forEach(a -> add(new ApplicationNode(a)));
    } catch (IOException e) {
      add(new DefaultMutableTreeNode("Failed to load applications"));
    }
  }

  @Override
  public String getIconName() {
    return "/images/project.png";
  }
}
