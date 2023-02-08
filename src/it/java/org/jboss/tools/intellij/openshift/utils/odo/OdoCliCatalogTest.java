/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;
import static org.junit.Assert.assertTrue;

public class OdoCliCatalogTest extends OdoCliTest {

    @Test
    public void checkGetComponentTypes() throws IOException, ExecutionException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            createProject(project);
            List<DevfileComponentType> components = odo.getComponentTypes();
            assertTrue(components.size() > 0);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkGetServiceTemplates() throws IOException, ExecutionException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            createProject(project);
            //After a namespace is created the cluster wide operators takes time to appear
            //in installed state into the namespace
            with().pollDelay(10, TimeUnit.SECONDS).await().atMost(10, TimeUnit.MINUTES).until(() -> !odo.getServiceTemplates().isEmpty());
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkMultiPlansServiceTemplates() throws IOException, ExecutionException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            createProject(project);
            //After a namespace is created the cluster wide operators takes time to appear
            //in installed state into the namespace
            with().pollDelay(10, TimeUnit.SECONDS).await().atMost(10, TimeUnit.MINUTES).until(() -> odo.getServiceTemplates().stream().anyMatch(template -> template.getCRDs().size() > 1));
        } finally {
            odo.deleteProject(project);
        }
    }
}
