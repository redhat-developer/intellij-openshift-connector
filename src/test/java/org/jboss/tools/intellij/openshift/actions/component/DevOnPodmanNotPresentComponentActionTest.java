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
import org.jboss.tools.intellij.openshift.tree.application.ApplicationRootNodeOdo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoDelegate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DevOnPodmanNotPresentComponentActionTest extends ActionTest {

  @Override
  protected @NotNull CompletableFuture<ApplicationRootNodeOdo> createOdoFuture() {
    CompletableFuture<ApplicationRootNodeOdo> future = super.createOdoFuture();
    ApplicationRootNodeOdo applicationRootNodeOdo = mock(ApplicationRootNodeOdo.class);
    when(applicationRootNodeOdo.getNamespaceKind()).thenReturn(isOpenshift ? "Project" : "Namespace");
    when(future.getNow(any())).thenReturn(applicationRootNodeOdo);
    OdoDelegate odo = mock(OdoDelegate.class);
    try {
      applicationRootNodeOdo = future.getNow(null);
      Field fld = applicationRootNodeOdo.getClass().getDeclaredField("delegate");
      fld.setAccessible(true);
      fld.set(applicationRootNodeOdo, odo);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    return future;
  }

  public DevOnPodmanNotPresentComponentActionTest(boolean isOpenshift) {
    super(isOpenshift);
  }

  @Override
  public AnAction getAction() {
    return new DevOnPodmanComponentAction();
  }

  @Override
  protected void verifyLocalDevOnPodmanComponentWithDevSupportedFeatures(@NotNull Presentation presentation) {
    assertFalse(presentation.isEnabled());
    assertTrue(presentation.isVisible());
    assertEquals("Stop dev on Podman", presentation.getText());
  }

  @Override
  protected void verifyLocalDevComponentWithDevSupportedFeatures(@NotNull Presentation presentation) {
    assertFalse(presentation.isEnabled());
    assertTrue(presentation.isVisible());
    assertEquals("Start dev on Podman", presentation.getText());
  }
}
