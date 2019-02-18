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

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.BaseTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Odo.class)
@PowerMockIgnore({"javax.net.ssl.*","javax.security.*","javax.swing.*"})
public class OdoTest extends BaseTest {

    private static Odo odo;

    private static OpenShiftClient client;

    private Random random = new Random();

    private static final String PROJECT_PREFIX = "test-project-";

    private static final String APPLICATION_PREFIX = "test-application-";

    @BeforeClass
    public static void init() throws IOException {
        PowerMockito.mockStatic(Odo.class);
        Mockito.when(Odo.isDownloadAllowed()).thenReturn(true);
        Mockito.when((Odo.get())).thenCallRealMethod();
        odo = Odo.get();
        client = new DefaultOpenShiftClient(new ConfigBuilder().build());
    }

    private void pause() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void checkCreateProject() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            odo.createProject(project);
            pause();
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
            odo.createProject(project);
            pause();
            odo.createApplication(project, application);
            pause();
        } finally {
            try {
                odo.deleteApplication(project, application);
            } catch (IOException e) {}
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
    public void checkListProjects() throws IOException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            odo.createProject(project);
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
        try {
            odo.createProject(project);
            pause();
            odo.createApplication(project, application);
            pause();
            odo.createComponentLocal(project, application, "redhat-openjdk18-openshift", "1.4", "comp", new File("src/it/projects/springboot-rest").getAbsolutePath());
        } finally {
            try {
                odo.deleteApplication(project, application);
            } catch (IOException e) {}
            try {
                odo.deleteProject(project);
            } catch (IOException e) {}
        }
    }

}
