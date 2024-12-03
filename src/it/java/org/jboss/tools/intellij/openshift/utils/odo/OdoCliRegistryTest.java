/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import java.io.IOException;
import java.util.List;

public class OdoCliRegistryTest extends OdoCliTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    odo.createDevfileRegistry(REGISTRY_NAME, REGISTRY_URL, null);
  }

  @Override
  protected void tearDown() throws Exception {
    odo.deleteDevfileRegistry(REGISTRY_NAME);
    super.tearDown();
  }

  public void testCheckCreateRegistry() throws IOException {
    String registryName = REGISTRY_PREFIX + random.nextInt();
    try {
      odo.createDevfileRegistry(registryName, "https://registry.devfile.io", null);
      List<DevfileRegistry> registries = odo.listDevfileRegistries();
      assertFalse(registries.isEmpty());
      assertEquals(1, registries.size());
    } finally {
      odo.deleteDevfileRegistry(registryName);
    }
  }

  public void testCheckListRegistries() throws IOException {
    List<DevfileRegistry> registries = odo.listDevfileRegistries();
    assertFalse(registries.isEmpty());
  }
}
