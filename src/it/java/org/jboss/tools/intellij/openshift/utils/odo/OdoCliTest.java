///*******************************************************************************
// * Copyright (c) 2019-2020 Red Hat, Inc.
// * Distributed under license by Red Hat, Inc. All rights reserved.
// * This program is made available under the terms of the
// * Eclipse Public License v2.0 which accompanies this distribution,
// * and is available at http://www.eclipse.org/legal/epl-v20.html
// *
// * Contributors:
// * Red Hat, Inc. - initial API and implementation
// ******************************************************************************/
//package org.jboss.tools.intellij.openshift.utils.odo;
//
//import com.intellij.openapi.ui.TestDialog;
//import com.redhat.devtools.intellij.common.utils.MessagesHelper;
//import io.fabric8.kubernetes.client.ConfigBuilder;
//import io.fabric8.kubernetes.client.DefaultKubernetesClient;
//import io.fabric8.openshift.client.OpenShiftClient;
//import org.apache.commons.io.FileUtils;
//import org.jboss.tools.intellij.openshift.BaseTest;
//import org.junit.After;
//import org.junit.Before;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Random;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//
//import static org.awaitility.Awaitility.with;
//import static org.junit.Assert.assertNotNull;
//
//
//public abstract class OdoCliTest extends BaseTest {
//
//    public static final String COMPONENT_PATH = "src/it/projects/springboot-rest";
//    public static final String SERVICE_TEMPLATE = "cloud-native-postgresql.v1.10.0";
//    public static final String SERVICE_CRD = "clusters.postgresql.k8s.enterprisedb.io";
//    public static final String REGISTRY_URL = "https://registry.devfile.io";
//    public static final String REGISTRY_NAME = "RegistryForITTests";
//
//    protected Odo odo;
//
//    protected Random random = new Random();
//
//    protected static final String PROJECT_PREFIX = "prj";
//
//    protected static final String APPLICATION_PREFIX = "app";
//
//    protected static final String COMPONENT_PREFIX = "comp";
//
//    protected static final String SERVICE_PREFIX = "srv";
//
//    protected static final String STORAGE_PREFIX = "stor";
//
//    protected static final String REGISTRY_PREFIX = "reg";
//
//    protected static final String CLUSTER_URL = System.getenv("CLUSTER_URL");
//
//    protected static final String CLUSTER_USER = System.getenv("CLUSTER_USER");
//
//    protected static final String CLUSTER_PASSWORD = System.getenv("CLUSTER_PASSWORD");
//
//    private TestDialog previousTestDialog;
//
//    protected static boolean isOpenShift() {
//        return new DefaultKubernetesClient(new ConfigBuilder().build()).isAdaptable(OpenShiftClient.class);
//    }
//
//    @Before
//    public void init() throws IOException, ExecutionException, InterruptedException {
//        previousTestDialog = MessagesHelper.setTestDialog(TestDialog.OK);
//        odo = OdoCliFactory.getInstance().getOdo(project).get();
//        if (odo.listDevfileRegistries().stream().noneMatch(c -> c.getName().equals(REGISTRY_NAME)))
//            odo.createDevfileRegistry(REGISTRY_NAME, REGISTRY_URL, null);
//
//        if (CLUSTER_URL != null && !odo.getMasterUrl().toString().startsWith(CLUSTER_URL)) {
//            odo.login(CLUSTER_URL, CLUSTER_USER, CLUSTER_PASSWORD.toCharArray(), null);
//            odo = OdoCliFactory.getInstance().getOdo(project).get();
//        }
//    }
//
//    @After
//    public void shutdown() throws IOException {
//        MessagesHelper.setTestDialog(previousTestDialog);
//        odo.deleteDevfileRegistry(REGISTRY_NAME);
//    }
//
//    protected void createProject(String project) throws IOException {
//        odo.createProject(project);
//    }
//
//    protected void createComponent(String project, String application, String component, boolean push) throws IOException {
//        createProject(project);
//        cleanLocalProjectDirectory();
//        odo.createComponent(project, application, "java-springboot", REGISTRY_NAME, component, new File(COMPONENT_PATH).getAbsolutePath(), null, null, push);
//    }
//
//    protected void createStorage(String project, String application, String component, boolean push, String storage) throws IOException {
//        createComponent(project, application, component, push);
//        odo.createStorage(project, application, COMPONENT_PATH, component, storage, "/tmp", "1Gi");
//    }
//
//    private void cleanLocalProjectDirectory() throws IOException {
//        FileUtils.deleteDirectory(new File(COMPONENT_PATH, ".odo"));
//        FileUtils.deleteQuietly(new File(COMPONENT_PATH+"/devfile.yaml"));
//    }
//
//    protected OperatorCRD getOperatorCRD(ServiceTemplate serviceTemplate) {
//        OperatorCRD crd = serviceTemplate.getCRDs().stream().filter(c -> c.getName().equals(SERVICE_CRD)).findFirst().orElse(null);
//        assertNotNull(crd);
//        return crd;
//    }
//
//    protected ServiceTemplate getServiceTemplate() throws IOException {
//        with().pollDelay(10, TimeUnit.SECONDS).await().atMost(10, TimeUnit.MINUTES).until(() -> !odo.getServiceTemplates().isEmpty());
//        ServiceTemplate serviceTemplate = odo.getServiceTemplates().stream().filter(s -> s.getName().equals(SERVICE_TEMPLATE)).findFirst().orElse(null);
//        assertNotNull(serviceTemplate);
//        return serviceTemplate;
//    }
//}
