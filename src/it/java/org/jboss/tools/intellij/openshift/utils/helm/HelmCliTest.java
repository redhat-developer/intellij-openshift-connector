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
package org.jboss.tools.intellij.openshift.utils.helm;

import com.intellij.openapi.ui.TestDialog;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.redhat.devtools.intellij.common.utils.MessagesHelper;
import org.jboss.tools.intellij.openshift.utils.ToolFactory;

public abstract class HelmCliTest extends BasePlatformTestCase {

    private static final String OPENSHIFT_REPO_NAME = "openshift";
    private static final String OPENSHIFT_REPO_URL = "https://charts.openshift.io/";
    protected Helm helm;

    private TestDialog previousTestDialog;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.previousTestDialog = MessagesHelper.setTestDialog(TestDialog.OK);
        this.helm = ToolFactory.getInstance().getHelm(getProject()).get();
        this.helm.addRepo(OPENSHIFT_REPO_NAME, OPENSHIFT_REPO_URL);
    }

    @Override
    protected void tearDown() throws Exception {
        MessagesHelper.setTestDialog(previousTestDialog);
        super.tearDown();
    }
}
