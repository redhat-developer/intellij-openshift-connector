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

import com.intellij.openapi.ui.TestDialog;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.redhat.devtools.intellij.common.utils.MessagesHelper;
import org.apache.commons.io.FileUtils;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationRootNodeOdo;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.OdoCluster;
import org.jboss.tools.intellij.openshift.utils.ToolFactory;
import org.jboss.tools.intellij.openshift.utils.oc.Oc;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;
import static org.mockito.Mockito.mock;


public abstract class OdoCliTest extends BasePlatformTestCase {

  // see https://operatorhub.io/operator/cloud-native-postgresql/ STABLE channel for versions
  public static final String SERVICE_TEMPLATE = "cloud-native-postgresql";
  public static final String SERVICE_KUBE_VERSION = "v1.16.2";
  public static final String SERVICE_OPENSHIFT_VERSION = "v1.22.1";

  public static final String SERVICE_CRD = "clusters.postgresql.k8s.enterprisedb.io";
  public static final String REGISTRY_URL = "https://registry.stage.devfile.io";
  public static final String REGISTRY_NAME = "RegistryForITTests";

  protected OdoFacade odo;

  protected ApplicationsRootNode rootNode = mock(ApplicationsRootNode.class);

  private final OdoProcessHelper processHelper = new OdoProcessHelper();

  protected Random random = new Random();

  protected static final String PROJECT_PREFIX = "prj";

  protected static final String COMPONENT_PREFIX = "comp";

  protected static final String SERVICE_PREFIX = "srv";

  protected static final String REGISTRY_PREFIX = "reg";

  private TestDialog previousTestDialog;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    previousTestDialog = MessagesHelper.setTestDialog(TestDialog.OK);
    rootNode.getOcTool().whenComplete((ocTool, throwable) -> {
      try {
        Oc oc = ocTool.get();
        OdoCluster.INSTANCE.login(oc);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    odo = getOdo().get();
    if (odo.listDevfileRegistries().stream().noneMatch(c -> c.getName().equals(REGISTRY_NAME)))
      odo.createDevfileRegistry(REGISTRY_NAME, REGISTRY_URL, null);
  }

  @Override
  protected void tearDown() throws Exception {
    MessagesHelper.setTestDialog(previousTestDialog);
    odo.deleteDevfileRegistry(REGISTRY_NAME);
    super.tearDown();
  }

  private CompletableFuture<OdoFacade> getOdo() {
    return ToolFactory.getInstance()
      .createOdo(getProject())
      .thenApply(tool -> new ApplicationRootNodeOdo(tool.get(), false, rootNode, processHelper));
  }

  protected void createProject(String project) throws IOException, ExecutionException, InterruptedException {
    odo.createProject(project);
    odo = getOdo().get();
  }

  protected void createComponent(String project, String component, String starter, String projectPath) throws IOException, ExecutionException, InterruptedException {
    createProject(project);
    odo.createComponent("go", REGISTRY_NAME, component, projectPath
      , null, starter);
  }

  protected void createComponent(String project, String component, String projectPath) throws IOException, ExecutionException, InterruptedException {
    createComponent(project, component, null, projectPath);
  }

  protected void cleanLocalProjectDirectory(String projectPath) throws IOException {
    FileUtils.deleteDirectory(new File(projectPath, ".odo"));
    FileUtils.deleteDirectory(new File(projectPath, "kubernetes"));
    FileUtils.deleteQuietly(new File(projectPath + "/devfile.yaml"));
  }

  protected OperatorCRD getOperatorCRD(ServiceTemplate serviceTemplate) {
    OperatorCRD crd = serviceTemplate.getCRDs().stream().filter(c -> c.getName().equals(SERVICE_CRD)).findFirst().orElse(null);
    assertNotNull(crd);
    return crd;
  }

  protected ServiceTemplate getServiceTemplate() throws IOException {
    with().pollDelay(10, TimeUnit.SECONDS).await().atMost(5, TimeUnit.MINUTES).until(() -> !odo.getServiceTemplates().isEmpty());
    with().pollDelay(10, TimeUnit.SECONDS).await().atMost(5, TimeUnit.MINUTES).until(() -> odo.getServiceTemplates().stream().anyMatch(s -> s.getName().equals(SERVICE_TEMPLATE + "." + (odo.isOpenShift() ? SERVICE_OPENSHIFT_VERSION : SERVICE_KUBE_VERSION))));
    return odo.getServiceTemplates().stream().filter(s -> s.getName().equals(SERVICE_TEMPLATE + "." + (odo.isOpenShift() ? SERVICE_OPENSHIFT_VERSION : SERVICE_KUBE_VERSION))).findFirst().orElse(null);
  }

  protected void createService(String project, ServiceTemplate serviceTemplate, OperatorCRD crd, String service, String projectPath) throws IOException {
    cleanLocalProjectDirectory(projectPath);
    odo.createService(project, serviceTemplate, crd, service, null, false);
  }
}
