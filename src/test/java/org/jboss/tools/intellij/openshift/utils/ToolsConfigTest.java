/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class ToolsConfigTest {
  private static final URL url = ToolsConfigTest.class.getResource("/tools-test.json");
  @Test
  public void verifyThatConfigCanLoad() throws IOException {
    ToolsConfig config = ConfigHelper.loadToolsConfig(url);
    Assert.assertNotNull(config);
  }

  @Test
  public void verifyThatConfigReturnsTools() throws IOException {
    ToolsConfig config = ConfigHelper.loadToolsConfig(url);
    Assert.assertNotNull(config);
    ToolsConfig.Tool odo = config.getTools().get("odo");
    Assert.assertNotNull(odo);
  }

  @Test
  public void verifyThatConfigReturnsVersion() throws IOException {
    ToolsConfig config = ConfigHelper.loadToolsConfig(url);
    Assert.assertNotNull(config);
    ToolsConfig.Tool odo = config.getTools().get("odo");
    Assert.assertNotNull(odo);
    Assert.assertEquals("0.0.14", odo.getVersion());
  }
}
