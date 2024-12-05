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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class OdoCliRegistryTest extends OdoCliTest {

  @Before
  public void initTestEnv() throws IOException {
    odo.createDevfileRegistry(REGISTRY_NAME, REGISTRY_URL, null);
  }

  @After
  public void cleanupTestEnv() throws IOException {
    odo.deleteDevfileRegistry(REGISTRY_NAME);
  }

  @Test
  public void testCheckCreateRegistry() throws IOException {
    String registryName = REGISTRY_PREFIX + random.nextInt();
    try {
      List<DevfileRegistry> registries = odo.listDevfileRegistries();
      assertFalse(registries.isEmpty());
      odo.createDevfileRegistry(registryName, "https://registry.devfile.io", null);
      assertEquals(registries.size() + 1, odo.listDevfileRegistries().size());
      assertTrue(odo.listDevfileRegistries().stream().anyMatch(reg -> reg.getName().equals(registryName)));
    } finally {
      odo.deleteDevfileRegistry(registryName);
    }
  }

  @Test
  public void testCheckListRegistries() throws IOException {
    List<DevfileRegistry> registries = odo.listDevfileRegistries();
    assertFalse(registries.isEmpty());
  }
}
