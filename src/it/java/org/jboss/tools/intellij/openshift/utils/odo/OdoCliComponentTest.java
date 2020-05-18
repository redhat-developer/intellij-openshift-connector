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

import java.net.ServerSocket;

import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.runners.Parameterized.*;
import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;

@RunWith(Parameterized.class)
public class OdoCliComponentTest extends OdoCliTest {
    private boolean push;

    public OdoCliComponentTest(boolean push, String label) {
        this.push = push;
    }

    @Parameters(name = "{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {false, "not pushed"},
            {true, "pushed"}
        });

    }

    @Test
    public void checkCreateComponent() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateAndDeleteComponent() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
            odo.deleteComponent(project, application, COMPONENT_PATH, component, push);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    private void checkCreateComponentAndCreateURL(boolean secure) throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
            odo.createURL(project, application, COMPONENT_PATH, component, "url1", 8080, secure);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(1, urls.size());
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateComponentAndCreateURL() throws IOException, InterruptedException {
        checkCreateComponentAndCreateURL(false);
    }

    @Test
    public void checkCreateComponentAndCreateSecureURL() throws IOException, InterruptedException {
        checkCreateComponentAndCreateURL(true);
    }

    private void checkCreateComponentAndCreateAndDeleteURL(boolean secure) throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
            odo.createURL(project, application, COMPONENT_PATH, component, null, 8080, secure);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(1, urls.size());
            odo.deleteURL(project, application, COMPONENT_PATH, component, urls.get(0).getName());
            urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(0, urls.size());
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateComponentAndCreateAndDeleteURL() throws IOException, InterruptedException {
        checkCreateComponentAndCreateAndDeleteURL(false);
    }

    @Test
    public void checkCreateComponentAndCreateAndDeleteSecureURL() throws IOException, InterruptedException {
        checkCreateComponentAndCreateAndDeleteURL(true);
    }

    @Test
    @Ignore("not yet supported by odo")
    public void checkCreateComponentAndLinkService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
            odo.createService(project, application, "postgresql-persistent", "default", service);
            odo.link(project, application, component, COMPONENT_PATH, service, null);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateComponentAndCreateStorage() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        String storage = STORAGE_PREFIX + random.nextInt();
        try {
            createStorage(project, application, component, push, storage);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateComponentAndCreateDeleteStorage() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        String storage = STORAGE_PREFIX + random.nextInt();
        try {
            createStorage(project, application, component, push, storage);
            odo.deleteStorage(project, application, COMPONENT_PATH, component, storage);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateComponentAndListURLs() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(0, urls.size());
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateComponentAndDebug() throws IOException, InterruptedException {
        if (!push) {
            return;
        }
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
            odo.createURL(project, application, COMPONENT_PATH, component, "url1", 8080, false);
            odo.push(project, application, COMPONENT_PATH, component);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(1, urls.size());
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
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }
}
