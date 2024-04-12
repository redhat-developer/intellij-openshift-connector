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
package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.ActionTest;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;

import static org.mockito.Mockito.mock;

public class CreateProjectActionTest extends ActionTest {
  @Override
  public AnAction getAction() {
    return new CreateProjectAction();
  }

  @Override
  protected void verifyLoggedInCluster(boolean visible) {
    assertTrue(visible);
  }

  public void testActionOnProject() {
    NamespaceNode projectNode = mock(NamespaceNode.class);
    AnActionEvent event = createEvent(projectNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testActionOnLocalDevComponent() {
    ComponentNode componentNode = mock(ComponentNode.class);
    AnActionEvent event = createEvent(componentNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testActionOnService() {
    ServiceNode serviceNode = mock(ServiceNode.class);
    AnActionEvent event = createEvent(serviceNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }
}
