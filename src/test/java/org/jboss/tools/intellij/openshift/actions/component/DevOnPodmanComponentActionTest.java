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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import org.jboss.tools.intellij.openshift.actions.ActionTest;
import org.jetbrains.annotations.NotNull;

public class DevOnPodmanComponentActionTest extends ActionTest {
  public DevOnPodmanComponentActionTest(boolean isOpenshift) {
    super(isOpenshift);
  }

  @Override
  public AnAction getAction() {
    return new DevOnPodmanComponentAction();
  }

  @Override
  protected void verifyLocalDevOnPodmanComponentWithDevSupportedFeatures(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
    assertEquals("Stop dev on Podman", presentation.getText());
  }

  @Override
  protected void verifyLocalDevComponentWithDevSupportedFeatures(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
    assertEquals("Start dev on Podman", presentation.getText());
  }
}
