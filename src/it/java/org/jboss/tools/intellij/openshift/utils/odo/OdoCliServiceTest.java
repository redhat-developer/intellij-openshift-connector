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

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OdoCliServiceTest extends OdoCliTest {

    public static final String SERVICE_TEMPLATE = "postgres-operator.v1.4.0";
    private static final String SERVICE_CRD = "postgresql";

    @Test
    public void checkCreateService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, false, ComponentKind.DEVFILE);
            odo.createService(project, application, COMPONENT_PATH, SERVICE_TEMPLATE, SERVICE_CRD, service, false);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    @Ignore("getServiceTemplate not implemented")
    public void checkCreateServiceAndGetTemplate() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, false, ComponentKind.DEVFILE);
            odo.createService(project, application, COMPONENT_PATH, SERVICE_TEMPLATE, SERVICE_CRD, service, false);
            String template = odo.getServiceTemplate(project, application, service);
            assertNotNull(template);
            assertEquals(SERVICE_TEMPLATE, template);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateDeleteService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, false, ComponentKind.DEVFILE);
            odo.createService(project, application, COMPONENT_PATH, SERVICE_TEMPLATE, SERVICE_CRD, service, false);
            odo.deleteService(project, application, COMPONENT_PATH, SERVICE_CRD + '/' + service);
        } finally {
            odo.deleteProject(project);
        }
    }
}
