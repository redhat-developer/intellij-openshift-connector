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
            } catch (IOException e) {
            }
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
            } catch (IOException e) {
            }
        }
    }

    @Test
    public void checkCreateComponentAndCreateURL() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
            odo.createURL(project, application, COMPONENT_PATH, component, "url1", 8080);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(1, urls.size());
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {
            }
        }
    }

    @Test
    public void checkCreateComponentAndCreateAndDeleteURL() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push);
            odo.createURL(project, application, COMPONENT_PATH, component, null, 8080);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(1, urls.size());
            odo.deleteURL(project, application, COMPONENT_PATH, component, urls.get(0).getName());
            urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(0, urls.size());
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {
            }
        }
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
            } catch (IOException e) {
            }
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
            } catch (IOException e) {
            }
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
            } catch (IOException e) {
            }
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
            } catch (IOException e) {
            }
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
            odo.createURL(project, application, COMPONENT_PATH, component, "url1", 8080);
            odo.push(project, application, COMPONENT_PATH, component);
            List<URL> urls = odo.listURLs(project, application, COMPONENT_PATH, component);
            assertEquals(1, urls.size());
            URL url = urls.get(0);
            int debugPort;
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                debugPort = serverSocket.getLocalPort();
            }
            ExecHelper.submit(() -> {
                try {
                    odo.debug(project, application, COMPONENT_PATH, debugPort);
                } catch (IOException e) {
                    fail("should not raise IOE");
                }
            });

        } finally {
            odo.deleteProject(project);
        }
    }
}
