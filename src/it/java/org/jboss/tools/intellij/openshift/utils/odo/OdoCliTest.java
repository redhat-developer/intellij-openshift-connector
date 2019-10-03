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

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.apache.commons.io.FileUtils;
import org.jboss.tools.intellij.openshift.BaseTest;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Random;


public abstract class OdoCliTest extends BaseTest {

    public static final String COMPONENT_PATH = "src/it/projects/springboot-rest";
    protected Odo odo;

    protected OpenShiftClient client;

    protected Random random = new Random();

    protected static final String PROJECT_PREFIX = "prj";

    protected static final String APPLICATION_PREFIX = "app";

    protected static final String COMPONENT_PREFIX = "comp";

    protected static final String SERVICE_PREFIX = "srv";

    protected static final String STORAGE_PREFIX = "stor";

    @Before
    public void init() throws Exception {
        System.setProperty(OdoCli.ODO_DOWNLOAD_FLAG, Boolean.TRUE.toString());
        odo = OdoCli.get();
        client = new DefaultOpenShiftClient(new ConfigBuilder().build());
    }

    private void pause() throws InterruptedException {
        Thread.sleep(1000);
    }

    protected void createProject(String project) throws IOException, InterruptedException {
        odo.createProject(project);
        pause();
    }

    protected void createComponent(String project, String application, String component, boolean push) throws IOException, InterruptedException {
        createProject(project);
        FileUtils.deleteDirectory(new File(COMPONENT_PATH, ".odo"));
        odo.createComponentLocal(project, application, "java", "8", component, new File(COMPONENT_PATH).getAbsolutePath(), push);
    }

    protected void createStorage(String project, String application, String component, boolean push, String storage) throws IOException, InterruptedException {
        createComponent(project, application, component, push);
        odo.createStorage(project, application, COMPONENT_PATH, component, storage, "/tmp", "1Gi");
    }
}
