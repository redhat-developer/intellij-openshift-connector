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

public class LinkComponentActionTest extends ActionTest {
  public LinkComponentActionTest(boolean isOpenshift) {
    super(isOpenshift);
  }

  @Override
  public AnAction getAction() {
    return new LinkComponentAction();
  }

  @Override
  protected void verifyLocalDevComponentWithNoSupportedFeatures(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

  @Override
  protected void verifyLocalOnlyComponent(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

  @Override
  protected void verifyRemoteOnlyDevComponent(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

  @Override
  protected void verifyRemoteOnlyDevComponentWithoutContext(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

  @Override
  protected void verifyRemoteDeployComponent(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

  @Override
  protected void verifyLocalDevOnPodmanComponentWithNoSupportedFeatures(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

  @Override
  protected void verifyLocalDevOnPodmanComponentWithDevSupportedFeatures(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

  @Override
  protected void verifyLocalDevComponentWithDevSupportedFeatures(@NotNull Presentation presentation) {
    assertTrue(presentation.isVisible());
  }

}
