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
package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import org.jboss.tools.intellij.openshift.actions.ActionTest;
import org.jetbrains.annotations.NotNull;

public class ChangeActiveProjectActionTest extends ActionTest {
  public ChangeActiveProjectActionTest(boolean isOpenshift) {
    super(isOpenshift);
  }

  @Override
  public AnAction getAction() {
    return new ChangeActiveProjectAction();
  }

  @Override
  protected void verifyProject(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

  @Override
  protected void verifyLoggedInCluster(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
    assertEquals("Change " + (isOpenshift ? "Project" : "Namespace"), presentation.getText());
  }

}
