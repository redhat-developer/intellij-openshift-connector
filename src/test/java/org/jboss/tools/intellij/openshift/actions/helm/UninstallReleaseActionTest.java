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
package org.jboss.tools.intellij.openshift.actions.helm;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import org.jboss.tools.intellij.openshift.actions.ActionTest;
import org.jetbrains.annotations.NotNull;

public class UninstallReleaseActionTest extends ActionTest {
  public UninstallReleaseActionTest(boolean isOpenshift) {
    super(isOpenshift);
  }

  @Override
  public AnAction getAction() {
    return new UninstallReleaseAction();
  }

  @Override
  protected void verifyChartRelease(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }
}
