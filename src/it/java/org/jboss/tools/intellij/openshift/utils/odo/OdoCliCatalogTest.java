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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;

public class OdoCliCatalogTest extends OdoCliTest {

  public void testCheckGetComponentTypes() throws IOException, ExecutionException, InterruptedException {
    String project = PROJECT_PREFIX + random.nextInt();
    try {
      createProject(project);
      List<DevfileComponentType> components = odo.getAllComponentTypes();
      assertFalse(components.isEmpty());
    } finally {
      odo.deleteProject(project);
    }
  }

  public void testCheckGetServiceTemplates() throws IOException, ExecutionException, InterruptedException {
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

  public void testCheckMultiPlansServiceTemplates() throws IOException, ExecutionException, InterruptedException {
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
