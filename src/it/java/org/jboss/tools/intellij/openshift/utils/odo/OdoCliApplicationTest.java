///*******************************************************************************
// * Copyright (c) 2020 Red Hat, Inc.
// * Distributed under license by Red Hat, Inc. All rights reserved.
// * This program is made available under the terms of the
// * Eclipse Public License v2.0 which accompanies this distribution,
// * and is available at http://www.eclipse.org/legal/epl-v20.html
// *
// * Contributors:
// * Red Hat, Inc. - initial API and implementation
// ******************************************************************************/
//package org.jboss.tools.intellij.openshift.utils.odo;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.Assert.assertTrue;
//
//@RunWith(Parameterized.class)
//public class OdoCliApplicationTest extends OdoCliTest {
//    private boolean push;
//
//    public OdoCliApplicationTest(boolean push) {
//        this.push = push;
//    }
//
//    @Parameterized.Parameters(name = "pushed: {0}")
//    public static Iterable<? extends Object> data() {
//        return Arrays.asList(false, true);
//    }
//
//    @Test
//    public void checkListApplications() throws IOException {
//        String project = PROJECT_PREFIX + random.nextInt();
//        String application = APPLICATION_PREFIX + random.nextInt();
//        String component = COMPONENT_PREFIX + random.nextInt();
//        try {
//            createComponent(project, application, component, push);
//            List<Application> applications = odo.getApplications(project);
//            assertTrue(push ? applications.size() > 0 : applications.size() == 0);
//        } finally {
//            odo.deleteProject(project);
//        }
//    }
//}
