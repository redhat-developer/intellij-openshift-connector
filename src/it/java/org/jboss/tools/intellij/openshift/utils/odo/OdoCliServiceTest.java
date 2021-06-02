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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OdoCliServiceTest extends OdoCliTest {

    public static final String SERVICE_TEMPLATE = "jenkins-pipeline-example";

    @Test
    public void checkCreateService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            if (odo.isServiceCatalogAvailable()) {
                odo.createService(project, application, SERVICE_TEMPLATE, "default", service, false);
            }
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateServiceAndGetTemplate() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            if (odo.isServiceCatalogAvailable()) {
                odo.createService(project, application, SERVICE_TEMPLATE, "default", service,false);
                String template = odo.getServiceTemplate(project, application, service);
                assertNotNull(template);
                assertEquals(SERVICE_TEMPLATE, template);
            }
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateDeleteService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            if (odo.isServiceCatalogAvailable()) {
                odo.createService(project, application, SERVICE_TEMPLATE, "default", service, false);
                odo.deleteService(project, application, service);
            }
        } finally {
            odo.deleteProject(project);
        }
    }
}
