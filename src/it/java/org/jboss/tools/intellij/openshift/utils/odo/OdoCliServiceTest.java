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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OdoCliServiceTest extends OdoCliTest {

    @Test
    public void checkCreateService() throws IOException {
        String project = PROJECT_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createService(project, service);
            List<Service> services = odo.getServices(project);
            assertNotNull(services);
            assertEquals(1, services.size());
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    @Ignore("getServiceTemplate not implemented")
    public void checkCreateServiceAndGetTemplate() throws IOException {
        String project = PROJECT_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createService(project, service);
            String template = odo.getServiceTemplate(project, service);
            assertNotNull(template);
            assertEquals(SERVICE_TEMPLATE, template);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    @Ignore("see https://github.com/redhat-developer/odo/issues/6347")
    public void checkCreateDeleteService() throws IOException {
        String project = PROJECT_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createService(project, service);
            List<Service> services = odo.getServices(project);
            assertNotNull(services);
            assertEquals(1, services.size());
            odo.deleteService(project, services.get(0));
            services = odo.getServices(project);
            assertNotNull(services);
            assertEquals(0, services.size());
        } finally {
            odo.deleteProject(project);
        }
    }

    private void createService(String project, String service) throws IOException {
        createProject(project);
        ServiceTemplate serviceTemplate = getServiceTemplate();
        OperatorCRD crd = getOperatorCRD(serviceTemplate);
        createService(project, serviceTemplate, crd, service);
    }
}
