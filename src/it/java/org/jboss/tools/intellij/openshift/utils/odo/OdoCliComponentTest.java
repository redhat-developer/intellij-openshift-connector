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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class OdoCliComponentTest extends OdoCliTest {
    private ComponentFeature feature;

    private String project;
    private String component;
    private String service;
    private String storage;
    private String host;

    public OdoCliComponentTest(ComponentFeature feature) {
        this.feature = feature;
    }

    @Parameterized.Parameters(name = "pushed: {0}")
    public static Iterable<? extends Object> data() {
        return Arrays.asList(null, ComponentFeature.DEV);
    }

    @Before
    public void initTestEnv() {
        project = PROJECT_PREFIX + random.nextInt();
        component = COMPONENT_PREFIX + random.nextInt();
        service = SERVICE_PREFIX + random.nextInt();
        storage = STORAGE_PREFIX + random.nextInt();
        host = odo.getMasterUrl().getHost();
    }

    @Test
    public void checkCreateComponent() throws IOException {
        try {
            createComponent(project, component, feature);
            List<Component> components = odo.getComponents(project);
            assertNotNull(components);
            assertEquals(feature == ComponentFeature.DEV? 1 : 0, components.size());
        } finally {
            odo.deleteProject(project);
        }
    }


    @Test
    public void checkCreateAndDiscoverComponent() throws IOException {
        try {
            createComponent(project, component, feature);
            List<ComponentDescriptor> components = odo.discover(COMPONENT_PATH);
            assertNotNull(components);
            assertEquals(1, components.size());
            assertEquals(new File(COMPONENT_PATH).getAbsolutePath(), components.get(0).getPath());
            assertEquals(component, components.get(0).getName());
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateAndDeleteComponent() throws IOException {
        try {
            createComponent(project, component, feature);
            odo.deleteComponent(project, COMPONENT_PATH, component, ComponentKind.DEVFILE);
        } finally {
            odo.deleteProject(project);
        }
    }

    private void checkCreateComponentAndCreateURL(boolean secure) throws IOException {
        try {
            createComponent(project, component, feature);
            odo.createURL(project, COMPONENT_PATH, component, "url1", 8000, secure, host);
            List<URL> urls = odo.listURLs(project, COMPONENT_PATH, component);
            assertEquals(odo.isOpenShift() ? 2 : 1, urls.size());
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndCreateURL() throws IOException {
        checkCreateComponentAndCreateURL(false);
    }

    @Test
    public void checkCreateComponentAndCreateSecureURL() throws IOException {
        checkCreateComponentAndCreateURL(true);
    }

    private void checkCreateComponentAndCreateAndDeleteURL(boolean secure) throws IOException {
        try {
            List<URL> urls;
            createComponent(project, component, feature);
            if (odo.isOpenShift()) {
                // remove url created automatically for openshift cluster to better visibility of the test
                urls = odo.listURLs(project, COMPONENT_PATH, component);
                assertEquals(1, urls.size());
                odo.deleteURL(project, COMPONENT_PATH, component, urls.get(0).getName());
                if (feature != null) {
                    odo.start(project, COMPONENT_PATH, component, feature);
                }
                urls = odo.listURLs(project, COMPONENT_PATH, component);
                assertEquals(0, urls.size());
            }

            odo.createURL(project, COMPONENT_PATH, component, null, 8000, secure, host);
            urls = odo.listURLs(project, COMPONENT_PATH, component);

            assertEquals(1, urls.size());
            assertEquals(URL.State.NOT_PUSHED, urls.get(0).getState());
            if (feature != null) {
                odo.start(project, COMPONENT_PATH, component, feature);
                urls = odo.listURLs(project, COMPONENT_PATH, component);
                assertEquals(1, urls.size());
                assertEquals(URL.State.PUSHED, urls.get(0).getState());
            }

            odo.deleteURL(project, COMPONENT_PATH, component, urls.get(0).getName());
            urls = odo.listURLs(project, COMPONENT_PATH, component);

            if (feature != null) {
                assertEquals(1, urls.size());
                assertEquals(URL.State.LOCALLY_DELETED, urls.get(0).getState());
                odo.start(project, COMPONENT_PATH, component, feature);
                urls = odo.listURLs(project, COMPONENT_PATH, component);
                assertEquals(0, urls.size());
            } else {
                assertEquals(0, urls.size());
            }

        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndCreateAndDeleteURL() throws IOException {
        checkCreateComponentAndCreateAndDeleteURL(false);
    }

    @Test
    public void checkCreateComponentAndCreateAndDeleteSecureURL() throws IOException {
        checkCreateComponentAndCreateAndDeleteURL(true);
    }

    @Test
    public void checkCreateComponentAndLinkService() throws IOException {
        Assume.assumeTrue(feature != null);
        try {
            createComponent(project, component, feature);
            ServiceTemplate serviceTemplate = getServiceTemplate();
            OperatorCRD crd = getOperatorCRD(serviceTemplate);
            odo.createService(project, serviceTemplate, crd, service, null, true);
            List<Service> deployedServices = odo.getServices(project);
            assertNotNull(deployedServices);
            assertEquals(1, deployedServices.size());
            Service deployedService = deployedServices.get(0);
            assertNotNull(deployedService);
            odo.link(project, COMPONENT_PATH, component, deployedService.getKind()+"/"+deployedService.getName());
            odo.start(project, COMPONENT_PATH, component, ComponentFeature.DEV);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndListURLs() throws IOException {
        try {
            createComponent(project, component, feature);
            List<URL> urls = odo.listURLs(project, COMPONENT_PATH, component);
            assertEquals(odo.isOpenShift() ? 1 : 0, urls.size());
            if (odo.isOpenShift()) {
                assertEquals(feature != null ? URL.State.PUSHED : URL.State.NOT_PUSHED, urls.get(0).getState());
            }
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndDebug() throws IOException {
        Assume.assumeTrue(feature != null);
        try {
            createComponent(project, component, feature);
            odo.createURL(project, COMPONENT_PATH, component, "url1", 8000, false, host);
            odo.start(project, COMPONENT_PATH, component, ComponentFeature.DEV);
            List<URL> urls = odo.listURLs(project, COMPONENT_PATH, component);
            assertEquals(odo.isOpenShift() ? 2 : 1, urls.size());
            int debugPort;
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                debugPort = serverSocket.getLocalPort();
            }
            ExecHelper.submit(() -> {
                try {
                    odo.debug(project, COMPONENT_PATH, component, debugPort);
                    DebugStatus status = odo.debugStatus(project, COMPONENT_PATH, component);
                    assertEquals(DebugStatus.RUNNING, status);
                } catch (IOException e) {
                    fail("Should not raise Exception");
                }
            });

        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentStarter() throws IOException {
        try {
            createProject(project);
            odo.createComponent(project, "java-springboot", REGISTRY_NAME, component,
                    Files.newTemporaryFolder().getAbsolutePath(), null, "springbootproject");
            List<Component> components = odo.getComponents(project);
            assertNotNull(components);
            assertEquals(feature != null ? 1 : 0, components.size());
        } finally {
            odo.deleteProject(project);
        }
    }
}
