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

import com.redhat.devtools.intellij.common.utils.ExecHelper;
import org.fest.util.Files;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;

@RunWith(Parameterized.class)
public class OdoCliComponentTest extends OdoCliTest {
  private final ComponentFeature feature;
  private String project;
  private String component;
  private String service;

  public OdoCliComponentTest(ComponentFeature feature) {
    this.feature = feature;
  }

  @Parameterized.Parameters(name = "feature: {0}")
  public static Iterable<?> data() {
    return Arrays.asList(null, ComponentFeature.DEV);
  }

  @Before
  public void initTestEnv() {
    project = PROJECT_PREFIX + random.nextInt();
    component = COMPONENT_PREFIX + random.nextInt();
    service = SERVICE_PREFIX + random.nextInt();
  }

  @After
  public void cleanUp() throws IOException {
    if (odo.isStarted(COMPONENT_PATH, ComponentFeature.DEV)) {
      odo.stop(COMPONENT_PATH, component, ComponentFeature.DEV);
    }
    if (project.equals(odo.getCurrentNamespace())) {
      odo.deleteProject(project);
    }
  }

  @Test
  public void checkCreateComponent() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, feature);
    List<Component> components = odo.getComponents(project);
    assertNotNull(components);
    assertEquals(feature == ComponentFeature.DEV ? 1 : 0, components.size());
  }

  @Test
  public void checkCreateAndDiscoverComponent() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, feature);
    List<ComponentDescriptor> components = odo.discover(COMPONENT_PATH);
    assertNotNull(components);
    assertEquals(1, components.size());
    assertEquals(new File(COMPONENT_PATH).getAbsolutePath(), components.get(0).getPath());
    assertEquals(component, components.get(0).getName());
  }

  @Test
  public void checkCreateAndDeleteComponent() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, feature);
    odo.deleteComponent(project, COMPONENT_PATH, component, ComponentKind.DEVFILE);
  }

  @Test
  @Ignore
  public void checkCreateComponentAndLinkService() throws IOException, ExecutionException, InterruptedException {
    Assume.assumeTrue(feature != null);
    createComponent(project, component, feature);
    ServiceTemplate serviceTemplate = getServiceTemplate();
    OperatorCRD crd = getOperatorCRD(serviceTemplate);
    odo.createService(project, serviceTemplate, crd, service, null, true);
    List<Service> deployedServices = odo.getServices(project);
    assertNotNull(deployedServices);
    assertEquals(1, deployedServices.size());
    Service deployedService = deployedServices.get(0);
    assertNotNull(deployedService);
    Binding binding = odo.link(COMPONENT_PATH, deployedService.getKind() + "/" + deployedService.getName());
    assertNotNull(binding);
  }

  @Test
  public void checkCreateComponentAndListURLs() throws IOException, ExecutionException, InterruptedException {
    Assume.assumeTrue(feature != null);
    createComponent(project, component, feature);
    List<URL> urls = odo.listURLs(COMPONENT_PATH);
    assertEquals(1, urls.size());
  }

  @Test
  public void checkCreateComponentAndDebug() throws IOException, ExecutionException, InterruptedException {
    Assume.assumeTrue(feature != null);
    createComponent(project, component, feature);
    odo.start(COMPONENT_PATH, component, ComponentFeature.DEV, null, null);
    List<URL> urls = odo.listURLs(COMPONENT_PATH);
    assertEquals(odo.isOpenShift() ? 2 : 1, urls.size());
    int debugPort;
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      debugPort = serverSocket.getLocalPort();
    }
    ExecHelper.submit(() -> {
      try {
        odo.debug(COMPONENT_PATH, debugPort);
        DebugStatus status = odo.debugStatus(COMPONENT_PATH);
        assertEquals(DebugStatus.RUNNING, status);
      } catch (IOException e) {
        fail("Should not raise Exception");
      }
    });
  }

  @Test
  public void checkCreateComponentStarter() throws IOException, ExecutionException, InterruptedException {
    createProject(project);
    odo.createComponent("go", REGISTRY_NAME, component,
      Files.newTemporaryFolder().getAbsolutePath(), null, "go-starter");
    List<Component> components = odo.getComponents(project);
    assertNotNull(components);
    assertEquals(0, components.size());
  }
}
