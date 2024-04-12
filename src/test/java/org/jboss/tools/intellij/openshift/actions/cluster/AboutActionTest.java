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
package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnAction;
import org.jboss.tools.intellij.openshift.actions.ActionTest;

public class AboutActionTest extends ActionTest {
  @Override
  public AnAction getAction() {
    return new AboutAction();
  }

  @Override
  protected void verifyLoggedInCluster(boolean visible) {
    assertTrue(visible);
  }

  @Override
  protected void verifyLoggedOutCluster(boolean visible) {
    assertTrue(visible);
  }
}
