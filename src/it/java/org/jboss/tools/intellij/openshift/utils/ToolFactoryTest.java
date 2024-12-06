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
package org.jboss.tools.intellij.openshift.utils;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jboss.tools.intellij.openshift.utils.ToolFactory.Tool;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.oc.Oc;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoDelegate;

import java.util.concurrent.ExecutionException;
import org.mockito.Mockito;

public class ToolFactoryTest extends BasePlatformTestCase {

  public void testGetOdo() throws ExecutionException, InterruptedException {
    Tool<OdoDelegate> tool = ToolFactory.getInstance().createOdo(Mockito.mock(), getProject()).get();
    Odo odo = tool.get();
    assertNotNull(odo);
  }

  public void testGetHelm() throws ExecutionException, InterruptedException {
    Tool<Helm> tool = ToolFactory.getInstance().createHelm().get();
    Helm helm = tool.get();
    assertNotNull(helm);
  }

  public void testGetOc() throws ExecutionException, InterruptedException {
    Tool<Oc> tool = ToolFactory.getInstance().createOc(Mockito.mock()).get();
    Oc oc = tool.get();
    assertNotNull(oc);
  }

}
