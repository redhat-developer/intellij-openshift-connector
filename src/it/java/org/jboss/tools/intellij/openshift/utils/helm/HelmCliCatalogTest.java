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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HelmCliCatalogTest extends HelmCliTest {

    public void testCheckGetComponentTypes() throws IOException, ExecutionException, InterruptedException {
        List<Chart> charts = helm.search();
        assertTrue(charts.size() > 0);
    }
}
