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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OdoCliServiceTest extends OdoCliTest {

    @Test
    public void checkCreateService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            if (odo.isServiceCatalogAvailable(client)) {
                odo.createService(project, application, "postgresql-persistent", "default", service);
            }
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateServiceAndGetTemplate() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            if (odo.isServiceCatalogAvailable(client)) {
                odo.createService(project, application, "postgresql-persistent", "default", service);
                String template = odo.getServiceTemplate(client, project, application, service);
                assertNotNull(template);
                assertEquals("postgresql-persistent", template);
            }
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateDeleteService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            if (odo.isServiceCatalogAvailable(client)) {
                odo.createService(project, application, "postgresql-persistent", "default", service);
                odo.deleteService(project, application, service);
            }
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }
}
