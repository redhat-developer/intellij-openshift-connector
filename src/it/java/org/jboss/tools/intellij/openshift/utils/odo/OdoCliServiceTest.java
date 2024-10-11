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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class OdoCliServiceTest extends OdoCliTest {

  private final String projectPath = new File("src/it/projects/springboot-rest").getAbsolutePath();

  @Override
  protected void tearDown() throws Exception {
    cleanLocalProjectDirectory(projectPath);
    super.tearDown();
  }

  public void testCheckCreateService() throws IOException, ExecutionException, InterruptedException {
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

  public void testCheckCreateDeleteService() throws IOException, ExecutionException, InterruptedException {
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

  private void createService(String project, String service) throws IOException, ExecutionException, InterruptedException {
    createProject(project);
    ServiceTemplate serviceTemplate = getServiceTemplate();
    OperatorCRD crd = getOperatorCRD(serviceTemplate);
    assertNotNull(crd);
    createService(project, serviceTemplate, crd, service);
  }
}
