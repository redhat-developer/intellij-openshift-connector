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

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class OdoCliProjectTest extends OdoCliTest {

    @Test
    public void testCheckCreateProject() throws IOException, ExecutionException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            List<String> projects = odo.getNamespaces();
            assertFalse(projects.isEmpty());
            createProject(project);
            assertEquals(projects.size()+1, odo.getNamespaces().size());
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void testCheckListProjects() throws IOException, ExecutionException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        try {
            createProject(project);
            List<String> projects = odo.getNamespaces();
            assertFalse(projects.isEmpty());
            assertTrue(projects.contains(project));
        } finally {
            odo.deleteProject(project);
        }
    }
}
