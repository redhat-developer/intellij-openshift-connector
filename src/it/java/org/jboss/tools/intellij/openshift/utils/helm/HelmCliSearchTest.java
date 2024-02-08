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


import java.io.IOException;
import java.util.List;

public class HelmCliSearchTest extends HelmCliTest {

    public void testSearch_should_list_all_charts() throws IOException {
        // given openshift a repo was added to helm
        // when
        List<Chart> charts = helm.search();
        // then
        assertTrue(charts.size() > 0);
    }

    public void testSearch_should_list_kuberos() throws IOException {
        // given openshift repo was added to helm
        String id = Charts.CHART_KUBEROS;
        // when
        List<Chart> charts = helm.search(id);
        // then
        boolean found = charts.stream().anyMatch((Chart chart) -> chart.getName().contains(id));
        assertTrue(found);
    }

}
