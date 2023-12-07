/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.helm;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jboss.tools.intellij.openshift.utils.OdoCluster;
import org.jboss.tools.intellij.openshift.utils.ToolFactory;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.util.Random;

public abstract class HelmCliTest extends BasePlatformTestCase {

    protected Helm helm;

    private String projectName = "prj-" + new Random().nextInt();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Odo odo = ToolFactory.getInstance().createOdo(getProject()).get();
        OdoCluster.INSTANCE.login(odo);
        odo.createProject(projectName);
        this.helm = ToolFactory.getInstance().createHelm(getProject()).get();
        Charts.addRepository(Charts.REPOSITORY_STABLE, helm);
    }

    @Override
    protected void tearDown() throws Exception {
        Odo odo = ToolFactory.getInstance().createOdo(getProject()).get();
        odo.deleteProject(projectName);
        super.tearDown();
    }

    protected void safeUninstall(String releaseName) {
        try {
            helm.uninstall(releaseName);
        } catch (Exception e) {
            LOG.info("Could not uninstall release " + releaseName, e);
        }
    }

}
