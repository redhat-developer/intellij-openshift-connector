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
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Random;
import org.jboss.tools.intellij.openshift.utils.KubernetesClientFactory;
import org.jboss.tools.intellij.openshift.utils.OdoCluster;
import org.jboss.tools.intellij.openshift.utils.ToolFactory;
import org.jboss.tools.intellij.openshift.utils.ToolFactory.Tool;
import org.jboss.tools.intellij.openshift.utils.oc.Oc;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoDelegate;

public abstract class HelmCliTest extends BasePlatformTestCase {

  protected Helm helm;
  private KubernetesClient client;
  private final String projectName = "prj-" + new Random().nextInt();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.client = new KubernetesClientFactory().create();
    Tool<OdoDelegate> odoTool = ToolFactory.getInstance().createOdo(client, getProject()).get();
    Tool<Oc> ocTool = ToolFactory.getInstance().createOc(client).get();
    Odo odo = odoTool.get();
    OdoCluster.INSTANCE.login(ocTool.get());
    odo.createProject(projectName);
    Tool<Helm> helmTool = ToolFactory.getInstance().createHelm().get();
    this.helm = helmTool.get();
    Charts.addRepository(Charts.REPOSITORY_STABLE, helm);
  }

  @Override
  protected void tearDown() throws Exception {
    Tool<OdoDelegate> tool = ToolFactory.getInstance().createOdo(client, getProject()).getNow(null);
    if (tool != null) {
      tool.get().deleteProject(projectName);
    }
    Charts.removeRepository(Charts.REPOSITORY_STABLE, helm);
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
