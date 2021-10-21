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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class OdoCliComponentTest extends OdoCliTest {
    private boolean push;
    private ComponentKind kind;

    private String project;
    private String application;
    private String component;
    private String service;
    private String storage;

    public OdoCliComponentTest(boolean push, ComponentKind kind) {
        this.push = push;
        this.kind = kind;
    }

    @Parameterized.Parameters(name = "pushed: {0}, kind: {1}")
    public static Iterable<Object[]> data() {
        if (isOpenShift()) {
            return Arrays.asList(new Object[][]{
                    {false, ComponentKind.S2I},
                    {true, ComponentKind.S2I},
                    {false, ComponentKind.DEVFILE},
                    {true, ComponentKind.DEVFILE}
            });
        } else {
            return Arrays.asList(new Object[][]{
                    {false, ComponentKind.DEVFILE},
                    {true, ComponentKind.DEVFILE}
            });
        }
    }

    @Before
    public void initTestEnv() {
        project = PROJECT_PREFIX + random.nextInt();
        application = APPLICATION_PREFIX + random.nextInt();
        component = COMPONENT_PREFIX + random.nextInt();
        service = SERVICE_PREFIX + random.nextInt();
        storage = STORAGE_PREFIX + random.nextInt();
    }

    @Test
    public void checkCreateComponent() throws IOException {
        try {
            createComponent(project, application, component, push, kind);
            List<Component> components = odo.getComponents(project, application);
            assertNotNull(components);
            assertEquals(push ? 1 : 0, components.size());
        } finally {
            odo.deleteProject(project);
        }
    }


    @Test
    public void checkCreateAndDiscoverComponent() throws IOException {
        try {
            createComponent(project, application, component, push, kind);
            List<ComponentDescriptor> components = odo.discover(COMPONENT_PATH);
            assertNotNull(components);
            assertEquals(1, components.size());
            assertEquals(new File(COMPONENT_PATH).getAbsolutePath(), components.get(0).getPath());
            assertEquals(component, components.get(0).getName());
            assertEquals(application, components.get(0).getApplication());
            assertEquals(project, components.get(0).getProject());
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateAndDeleteComponent() throws IOException {
        try {
            createComponent(project, application, component, push, kind);
            odo.deleteComponent(project, application, COMPONENT_PATH, component, kind);
        } finally {
            odo.deleteProject(project);
        }
    }

    private void checkCreateComponentAndCreateURL(boolean secure) throws IOException {
        try {
            createComponent(project, application, component, push, kind);
            odo.createURL(project, application, COMPONENT_PATH, component, "url1", 8080, secure);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            if (kind.equals(ComponentKind.DEVFILE)) {
                assertEquals(2, urls.size());
            } else {
                assertEquals(4, urls.size());
            }
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndCreateURL() throws IOException {
        Assume.assumeTrue(isOpenShift());
        checkCreateComponentAndCreateURL(false);
    }

    @Test
    public void checkCreateComponentAndCreateSecureURL() throws IOException {
        Assume.assumeTrue(isOpenShift());
        checkCreateComponentAndCreateURL(true);
    }

    private void checkCreateComponentAndCreateAndDeleteURL(boolean secure) throws IOException {
        try {
            createComponent(project, application, component, push, kind);
            odo.createURL(project, application, COMPONENT_PATH, component, null, 8080, secure);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            if (kind.equals(ComponentKind.DEVFILE)) {
                assertEquals(2, urls.size());
                assertEquals(push ? URL.State.PUSHED : URL.State.NOT_PUSHED, urls.get(0).getState());
                assertEquals(URL.State.NOT_PUSHED, urls.get(1).getState());
            } else {
                assertEquals(4, urls.size());
                assertEquals(URL.State.NOT_PUSHED, urls.get(0).getState());
            }
            odo.deleteURL(project, application, COMPONENT_PATH, component, urls.get(0).getName());
            urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            if (kind.equals(ComponentKind.DEVFILE)) {
                if (push) {
                    assertEquals(2, urls.size());
                    assertEquals(URL.State.LOCALLY_DELETED, urls.get(0).getState());
                    assertEquals(URL.State.NOT_PUSHED, urls.get(1).getState());
                } else {
                    assertEquals(1, urls.size());
                    assertEquals(URL.State.NOT_PUSHED, urls.get(0).getState());
                }
            } else {
                assertEquals(3, urls.size());
            }
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndCreateAndDeleteURL() throws IOException {
        Assume.assumeTrue(isOpenShift());
        checkCreateComponentAndCreateAndDeleteURL(false);
    }

    @Test
    public void checkCreateComponentAndCreateAndDeleteSecureURL() throws IOException {
        Assume.assumeTrue(isOpenShift());
        checkCreateComponentAndCreateAndDeleteURL(true);
    }

    @Test
    public void checkCreateComponentAndLinkService() throws IOException {
        Assume.assumeTrue(push && kind == ComponentKind.S2I);// TODO remove kind test when link with devfile is supported.
        try {
            createComponent(project, application, component, push, kind);
            ServiceTemplate serviceTemplate = getServiceTemplate();
            OperatorCRD crd = getOperatorCRD(serviceTemplate);
            odo.createService(project, application, serviceTemplate, crd, service, null, true);
            odo.link(project, application, component, COMPONENT_PATH, service, null);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndCreateStorage() throws IOException {
        try {
            createStorage(project, application, component, push, kind, storage);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndCreateDeleteStorage() throws IOException {
        try {
            createStorage(project, application, component, push, kind, storage);
            odo.deleteStorage(project, application, COMPONENT_PATH, component, storage);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndListURLs() throws IOException {
        Assume.assumeTrue(isOpenShift());
        try {
            createComponent(project, application, component, push, kind);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            if (kind.equals(ComponentKind.DEVFILE)) {
                assertEquals(1, urls.size());
            } else {
                assertEquals(3, urls.size());
            }
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndDebug() throws IOException {
        Assume.assumeTrue(isOpenShift() && push);
        try {
            createComponent(project, application, component, push, kind);
            odo.createURL(project, application, COMPONENT_PATH, component, "url1", 8080, false);
            odo.push(project, application, COMPONENT_PATH, component);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            if (kind.equals(ComponentKind.DEVFILE)) {
                assertEquals(2, urls.size());
            } else {
                assertEquals(4, urls.size());
            }
            int debugPort;
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                debugPort = serverSocket.getLocalPort();
            }
            ExecHelper.submit(() -> {
                try {
                    odo.debug(project, application, COMPONENT_PATH, component, debugPort);
                    DebugStatus status = odo.debugStatus(project, application, COMPONENT_PATH, component);
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
        Assume.assumeThat(ComponentKind.DEVFILE, equalTo(kind));
        try {
            createProject(project);
            odo.createComponentLocal(project, application, "java-springboot", null, REGISTRY_NAME,component, Files.newTemporaryFolder().getAbsolutePath(), null, "springbootproject", push);
            List<Component> components = odo.getComponents(project, application);
            assertNotNull(components);
            assertEquals(push ? 1 : 0, components.size());
        } finally {
            odo.deleteProject(project);
        }
    }
}
