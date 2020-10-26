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

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class OdoCliApplicationTest extends OdoCliTest {
    private boolean push;

    public OdoCliApplicationTest(boolean push, String label) {
        this.push = push;
    }

    @Parameterized.Parameters(name = "{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false, "not pushed"},
                {true, "pushed"}
        });
    }

    @Test
    public void checkListApplications() throws IOException, InterruptedException {
        String project = PROJECT_PREFIX + random.nextInt();
        String application = APPLICATION_PREFIX + random.nextInt();
        String component = COMPONENT_PREFIX + random.nextInt();
        try {
            createS2iComponent(project, application, component, push);
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
