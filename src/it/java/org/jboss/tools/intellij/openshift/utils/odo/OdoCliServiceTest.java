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
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            ServiceTemplate serviceTemplate = getServiceTemplate();
            OperatorCRD crd = getOperatorCRD(serviceTemplate);
            odo.createService(project, application, serviceTemplate, crd, service, null, false);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    @Ignore("getServiceTemplate not implemented")
    public void checkCreateServiceAndGetTemplate() throws IOException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            ServiceTemplate serviceTemplate = getServiceTemplate();
            OperatorCRD crd = getOperatorCRD(serviceTemplate);
            odo.createService(project, application, serviceTemplate, crd, service, null, false);
            String template = odo.getServiceTemplate(project, application, service);
            assertNotNull(template);
            assertEquals(SERVICE_TEMPLATE, template);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateDeleteService() throws IOException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createProject(project);
            ServiceTemplate serviceTemplate = getServiceTemplate();
            OperatorCRD crd = getOperatorCRD(serviceTemplate);
            odo.createService(project, application, serviceTemplate, crd, service, null, false);
            List<Service> services = odo.getServices(project, application);
            assertNotNull(services);
            assertEquals(1, services.size());
            odo.deleteService(project, application, services.get(0));
        } finally {
            odo.deleteProject(project);
        }
    }
}
