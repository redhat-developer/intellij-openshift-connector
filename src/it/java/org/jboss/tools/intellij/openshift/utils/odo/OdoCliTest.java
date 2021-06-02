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
import com.redhat.devtools.intellij.common.utils.MessagesHelper;
import org.apache.commons.io.FileUtils;
import org.jboss.tools.intellij.openshift.BaseTest;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;


public abstract class OdoCliTest extends BaseTest {

    public static final String COMPONENT_PATH = "src/it/projects/springboot-rest";
    protected Odo odo;

    protected Random random = new Random();

    protected static final String PROJECT_PREFIX = "prj";

    protected static final String APPLICATION_PREFIX = "app";

    protected static final String COMPONENT_PREFIX = "comp";

    protected static final String SERVICE_PREFIX = "srv";

    protected static final String STORAGE_PREFIX = "stor";

    protected static final String REGISTRY_PREFIX = "reg";

    protected static final String CLUSTER_URL = System.getenv("CLUSTER_URL");

    protected static final String CLUSTER_USER = System.getenv("CLUSTER_USER");

    protected static final String CLUSTER_PASSWORD = System.getenv("CLUSTER_PASSWORD");

    private TestDialog previousTestDialog;

    @Before
    public void init() throws IOException, ExecutionException, InterruptedException {
        previousTestDialog = MessagesHelper.setTestDialog(TestDialog.OK);
        odo = OdoCliFactory.getInstance().getOdo(project).get();

        if (CLUSTER_URL != null && !odo.getMasterUrl().toString().startsWith(CLUSTER_URL)) {
            odo.login(CLUSTER_URL, CLUSTER_USER, CLUSTER_PASSWORD.toCharArray(), null);
            odo = OdoCliFactory.getInstance().getOdo(project).get();
        }
    }

    @After
    public void shutdown() {
        MessagesHelper.setTestDialog(previousTestDialog);
    }

    protected void createProject(String project) throws IOException, InterruptedException {
        odo.createProject(project);
    }

    protected void createS2iComponent(String project, String application, String component, boolean push) throws IOException, InterruptedException {
        createProject(project);
        cleanLocalProjectDirectory();
        odo.createComponentLocal(project, application, "java", "8", component, new File(COMPONENT_PATH).getAbsolutePath(), null, null, push);
    }

    protected void createDevfileComponent(String project, String application, String component, boolean push) throws IOException, InterruptedException {
        createProject(project);
        cleanLocalProjectDirectory();
        odo.createComponentLocal(project, application, "java-springboot", null, component, new File(COMPONENT_PATH).getAbsolutePath(), null, null, push);
    }

    protected void createComponent(String project, String application, String component, boolean push, ComponentKind kind) throws IOException, InterruptedException {
        switch (kind){
            case S2I:
                createS2iComponent(project,application, component, push); break;
            case DEVFILE:
                createDevfileComponent(project,application, component, push); break;
        }
    }

    protected void createStorage(String project, String application, String component, boolean push, String storage) throws IOException, InterruptedException {
        createS2iComponent(project, application, component, push);
        odo.createStorage(project, application, COMPONENT_PATH, component, storage, "/tmp", "1Gi");
    }

    private void cleanLocalProjectDirectory() throws IOException {
        FileUtils.deleteDirectory(new File(COMPONENT_PATH, ".odo"));
        FileUtils.deleteQuietly(new File(COMPONENT_PATH+"/devfile.yaml"));
    }
}
