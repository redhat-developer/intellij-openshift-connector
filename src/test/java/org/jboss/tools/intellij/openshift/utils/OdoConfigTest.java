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
package org.jboss.tools.intellij.openshift.utils;

import org.jboss.tools.intellij.openshift.utils.odo.OdoConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class OdoConfigTest {
  private static final URL url = OdoConfigTest.class.getResource("/odo-config-test.yaml");
  @Test
  public void verifyThatConfigCanLoad() throws IOException {
    OdoConfig config = ConfigHelper.loadOdoConfig(url);
    Assert.assertNotNull(config);
  }

  @Test
  public void verifyThatConfigReturnsApplications() throws IOException {
    OdoConfig config = ConfigHelper.loadOdoConfig(url);
    Assert.assertNotNull(config);
    List<OdoConfig.Application> applications = config.getActiveApplications();
    Assert.assertNotNull(applications);
    Assert.assertTrue(applications.size() > 0);
  }

  @Test
  public void verifyThatConfigReturnsActiveApplication() throws IOException {
    OdoConfig config = ConfigHelper.loadOdoConfig(url);
    Assert.assertNotNull(config);
    List<OdoConfig.Application> applications = config.getActiveApplications();
    Assert.assertNotNull(applications);
    Assert.assertTrue(applications.size() > 0);
    Optional<OdoConfig.Application> activeApp = applications.stream().filter(app -> app.isActive()).findFirst();
    Assert.assertTrue(activeApp.isPresent());
    Assert.assertEquals("testapp3", activeApp.get().getName());
    Assert.assertEquals("test3", activeApp.get().getProject());
    Assert.assertEquals("", activeApp.get().getActiveComponent());
  }
}
