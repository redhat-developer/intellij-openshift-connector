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

import com.intellij.util.io.ZipUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.with;

@RunWith(Parameterized.class)
public class OdoCliComponentTest extends OdoCliTest {
  private static final String PROJECT_NAME = "go";
  private static final String COMPONENT_PATH = "src/it/projects/";
  private final ComponentFeature feature;
  private String project;
  private String component;
  private String service;
  private final String projectPath = new File(COMPONENT_PATH + PROJECT_NAME).getAbsolutePath();

  public OdoCliComponentTest(ComponentFeature feature, String label) {
    this.feature = feature;
  }

  @Parameterized.Parameters(name = "feature: {1}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {ComponentFeature.DEV, ComponentFeature.DEV.getLabel()},
      {ComponentFeature.DEV_ON_PODMAN, ComponentFeature.DEV_ON_PODMAN.getLabel()}
    });
  }

  @BeforeClass
  public static void initTestProject() throws IOException {
    Path destDir = new File(COMPONENT_PATH).toPath();
    Path srcFile = new File(COMPONENT_PATH + PROJECT_NAME + ".zip").toPath();
    ZipUtil.extract(srcFile, destDir, null);
  }

  @AfterClass
  public static void deleteTestProject() throws IOException {
    FileUtils.deleteDirectory(new File(COMPONENT_PATH));
  }

  @Before
  public void initTestEnv() throws IOException {
    project = PROJECT_PREFIX + random.nextInt();
    component = COMPONENT_PREFIX + random.nextInt();
    service = SERVICE_PREFIX + random.nextInt();
    odo.createDevfileRegistry(REGISTRY_NAME, REGISTRY_URL, null);
  }

  @After
  public void cleanUp() throws IOException {
    if (odo.isStarted(component, feature)) {
      odo.stop(projectPath, component, feature);
    }
    if (!odo.discover(projectPath).isEmpty()) {
      odo.deleteComponent(project, projectPath, component, ComponentKind.DEVFILE);
    }
    if (project.equals(odo.getCurrentNamespace())) {
      odo.deleteProject(project);
    }
    cleanLocalProjectDirectory(projectPath);
    odo.deleteDevfileRegistry(REGISTRY_NAME);
  }

  protected void startComponent(String component, ComponentFeature feature) throws IOException {
    AtomicBoolean started = new AtomicBoolean();
    odo.start(projectPath, component, feature, started::getAndSet, null);
    with().pollDelay(10, TimeUnit.SECONDS).await().atMost(15, TimeUnit.MINUTES).untilTrue(started);
  }

  @Test
  public void checkCreateComponent() throws IOException, ExecutionException, InterruptedException {
    List<ComponentDescriptor> descriptors = odo.discover(projectPath);
    assertNotNull(descriptors);
    assertEquals(0, descriptors.size());
    createComponent(project, component, projectPath);
    descriptors = odo.discover(projectPath);
    assertNotNull(descriptors);
    assertEquals(1, descriptors.size());
  }

  @Test
  public void checkCreateAndDiscoverComponent() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, projectPath);
    List<ComponentDescriptor> components = odo.discover(projectPath);
    assertNotNull(components);
    assertEquals(1, components.size());
    assertEquals(projectPath, components.get(0).getPath());
    assertEquals(component, components.get(0).getName());
  }

  @Test
  public void checkCreateAndDeleteComponent() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, projectPath);
    List<ComponentDescriptor> descriptors = odo.discover(projectPath);
    assertNotNull(descriptors);
    assertEquals(1, descriptors.size());
    odo.deleteComponent(project, projectPath, component, ComponentKind.DEVFILE);
    descriptors = odo.discover(projectPath);
    assertNotNull(descriptors);
    assertEquals(0, descriptors.size());
  }

  @Test
  public void checkCreateComponentAndLinkService() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, projectPath);
    ServiceTemplate serviceTemplate = getServiceTemplate();
    OperatorCRD crd = getOperatorCRD(serviceTemplate);
    assertNotNull(crd);
    odo.createService(project, serviceTemplate, crd, service, null, true);
    List<Service> deployedServices = odo.getServices(project);
    assertNotNull(deployedServices);
    assertEquals(1, deployedServices.size());
    Service deployedService = deployedServices.get(0);
    assertNotNull(deployedService);
    String target = deployedService.getName() + '/' + deployedService.getKind() + "." + deployedService.getApiVersion();
    Binding binding = odo.link(projectPath, target);
    assertNotNull(binding);
    List<Binding> bindings = odo.listBindings(projectPath);
    assertNotNull(bindings);
    assertEquals(1, bindings.size());
    // cleanup
    odo.deleteBinding(projectPath, binding.getName());
    bindings = odo.listBindings(projectPath);
    assertNotNull(bindings);
    assertEquals(0, bindings.size());
    odo.deleteService(project, deployedService);
    deployedServices = odo.getServices(project);
    assertNotNull(deployedServices);
    assertEquals(0, deployedServices.size());
  }

  @Test
  public void checkCreateComponentAndListURLs() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, projectPath);
    List<URL> urls = odo.listURLs(projectPath);
    assertEquals(0, urls.size());
    startComponent(component, feature);
    urls = odo.listURLs(projectPath);
    assertEquals(1, urls.size());
    //cleanup
    odo.stop(projectPath, component, feature);
  }

  @Test
  public void checkCreateComponentAndDebug() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, projectPath);
    DebugComponentFeature debugComponentFeature = new DebugComponentFeature(feature);
    startComponent(component, debugComponentFeature);
    assertTrue(odo.isStarted(component, debugComponentFeature));
    int debugPort;
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      debugPort = serverSocket.getLocalPort();
    }
    ExecHelper.submit(() -> {
      try {
        odo.debug(projectPath, debugPort);
      } catch (IOException e) {
        fail("Should not raise Exception: " + e.getMessage());
      }
    });
    List<Component> components = odo.getComponents(project);
    assertNotNull(components);
    assertEquals(1, components.size());
    Component comp = components.get(0);
    assertNotNull(comp);
    ComponentFeatures features = comp.getLiveFeatures();
    assertNotNull(features);
    assertFalse(features.isEmpty());
    assertEquals(1, features.size());
    // assertTrue(features.isDebug());  odo issue, see #redhat-developer/odo/issues/7197
    assertTrue(features.isDev());
    //cleanup
    odo.stop(projectPath, component, feature);
  }

  @Test
  public void checkCreateComponentStarter() throws IOException, ExecutionException, InterruptedException {
    String starterPath = FileUtil.createTempDirectory("go-starter", "").getPath();
    createComponent(project, component, "go-starter", starterPath);
    List<ComponentDescriptor> descriptors = odo.discover(starterPath);
    assertNotNull(descriptors);
    assertEquals(1, descriptors.size());
  }

  @Test
  public void checkCreateComponentAndStartDev() throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, projectPath);
    startComponent(component, feature);
    assertTrue(odo.isStarted(component, feature));
    ComponentInfo info = odo.getComponentInfo(project, component, projectPath, ComponentKind.DEVFILE);
    assertNotNull(info.getSupportedFeatures());
    assertEquals(2, info.getSupportedFeatures().size());
    List<ComponentFeature.Mode> supportedFeatures = info.getSupportedFeatures();
    assertTrue(supportedFeatures.contains(ComponentFeature.Mode.DEV_MODE));
    assertTrue(supportedFeatures.contains(ComponentFeature.Mode.DEBUG_MODE));
    List<Component> components = odo.getComponents(project);
    assertNotNull(components);
    assertEquals(1, components.size());
    Component comp = components.get(0);
    assertNotNull(comp);
    ComponentFeatures features = comp.getLiveFeatures();
    assertNotNull(features);
    assertFalse(features.isEmpty());
    assertEquals(1, features.size());
    assertTrue(features.isDev());
    //cleanup
    odo.stop(projectPath, component, feature);
  }

}
