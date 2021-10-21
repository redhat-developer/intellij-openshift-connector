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

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class OdoCliRegistryTest extends OdoCliTest {


    @Test
    public void checkCreateRegistry() throws IOException {
        String registryName = REGISTRY_PREFIX + random.nextInt();
        try {
            odo.createDevfileRegistry(registryName, "https://registry.devfile.io", null);
        } finally {
            odo.deleteDevfileRegistry(registryName);
        }
    }

    @Test
    public void checkListRegistries() throws IOException {
        List<DevfileRegistry> registries = odo.listDevfileRegistries();
        assertTrue(registries.size() > 0);
    }
}
