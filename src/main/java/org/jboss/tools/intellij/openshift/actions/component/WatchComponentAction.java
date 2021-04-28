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

import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;

public class WatchComponentAction extends PushComponentAction {

  @Override
  protected String getTelemetryActionName() { return "watch component"; }

  @Override
  public boolean isVisible(Object selected) {
    boolean visible = super.isVisible(selected);
    if (visible) {
      visible = ((Component)((ComponentNode)selected).getComponent()).getState() == ComponentState.PUSHED;
    }
    return visible;
  }

  @Override
  protected String getActionName() {
    return "Watch";
  }

  @Override
  protected void process(Odo odo, String project, String application, Component component) throws IOException {
    odo.watch(project, application, component.getPath(), component.getName());
  }
}
