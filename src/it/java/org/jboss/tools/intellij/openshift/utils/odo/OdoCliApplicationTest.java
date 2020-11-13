/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class OdoCliApplicationTest extends OdoCliTest {
    private boolean push;
    private ComponentKind kind;

    public OdoCliApplicationTest(boolean push, ComponentKind kind) {
        this.push = push;
        this.kind = kind;
    }

    @Parameterized.Parameters(name = "pushed: {0}, kind: {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false, ComponentKind.S2I},
                {true, ComponentKind.DEVFILE},
                {false, ComponentKind.DEVFILE},
                {true, ComponentKind.DEVFILE}
        });
    }

    @Test
    public void checkListApplications() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createComponent(project, application, component, push, kind);
            List<Application> applications = odo.getApplications(project);
            assertTrue(push ? applications.size() > 0 : applications.size() == 0);
        } finally {
            try {
                odo.deleteProject(project);
            } catch (IOException e) {
            }
        }
    }
}
