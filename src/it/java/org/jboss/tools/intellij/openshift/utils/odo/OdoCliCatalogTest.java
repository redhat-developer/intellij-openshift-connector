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
package org.jboss.tools.intellij.openshift.utils.odo;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class OdoCliCatalogTest extends OdoCliTest {

    @Test
    public void checkGetComponentTypes() throws IOException {
        List<ComponentType> components = odo.getComponentTypes();
        assertTrue(components.size() > 0);
    }

    @Test
    public void checkGetServiceTemplates() throws IOException {
        List<ServiceTemplate> services = odo.getServiceTemplates();
        assertTrue(services.size() > 0);
    }
}
