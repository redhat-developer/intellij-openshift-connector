/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import com.intellij.openapi.progress.ProgressIndicator;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class OdoTest extends BaseTest {

    private static Odo odo;

    private static OpenShiftClient client;

    private Random random = new Random();

    private static final String PROJECT_PREFIX = "prj";

    private static final String APPLICATION_PREFIX = "app";

    private static final String COMPONENT_PREFIX = "comp";

    private static final String SERVICE_PREFIX = "srv";

    private static final String STORAGE_PREFIX = "stor";

    @Before
    public void init() throws Exception {
        System.setProperty(Odo.ODO_DOWNLOAD_FLAG, Boolean.TRUE.toString());
        odo = Odo.get();
        client = new DefaultOpenShiftClient(new ConfigBuilder().build());
    }

    private void pause() throws InterruptedException {
        Thread.sleep(1000);
    }

    private void createProject(String project) throws IOException, InterruptedException {
        odo.createProject(project);
        pause();
    }

    private void createApplication(String project, String application) throws IOException, InterruptedException {
        createProject(project);
        odo.createApplication(project, application);
    }

    private void createComponent(String project, String application, String component) throws IOException, InterruptedException {
        createApplication(project, application);
        odo.createComponentLocal(project, application, "redhat-openjdk18-openshift", "1.4", component, new File("src/it/projects/springboot-rest").getAbsolutePath());
    }

    private void createStorage(String project, String application, String component, String storage) throws IOException, InterruptedException {
        createComponent(project, application, component);
        odo.createStorage(project, application, component, storage, "/tmp", "1Gi");
    }

    @Test
    public void checkCreateProject() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            createProject(project);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateApplication() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        try {
            createApplication(project, application);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkGetComponentTypes() throws IOException {
        List<ComponentType> components = odo.getComponentTypes();
        Assert.assertTrue(components.size() > 0);
    }

    @Test
    public void checkGetServiceTemplates() throws IOException {
        List<ServiceTemplate> services = odo.getServiceTemplates();
        Assert.assertTrue(services.size() > 0);
    }

    @Test
    public void checkListProjects() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            createProject(project);
            List<Project> projects = odo.getProjects(client);
            Assert.assertTrue(projects.size() > 0);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateComponent() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component);
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
            createComponent(project, application, component);
            odo.deleteComponent(project, application, component);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateComponentAndCreateURL() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component);
            odo.createUrl(project, application, component, 8080);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
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
            createComponent(project, application, component);
            odo.createService(project, application, "postgresql-persistent", "default", service);
            odo.link(project, application, component, service, null);
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
            createStorage(project, application, component, storage);
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
            createStorage(project, application, component, storage);
            odo.deleteStorage(project, application, component, storage);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    public void checkCreateService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createApplication(project, application);
            odo.createService(project, application, "postgresql-persistent", "default", service);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

    @Test
    @Ignore("not yet supported by odo")
    public void checkCreateDeleteService() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String service = SERVICE_PREFIX + random.nextInt();
        try {
            createApplication(project, application);
            odo.createService(project, application, "postgresql-persistent", "default", service);
            odo.deleteService(project, application, service);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }
}
