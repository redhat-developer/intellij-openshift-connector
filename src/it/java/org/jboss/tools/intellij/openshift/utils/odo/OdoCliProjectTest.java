///*******************************************************************************
// * Copyright (c) 2019-2020 Red Hat, Inc.
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
//
//import java.io.IOException;
//import java.util.List;
//
//import static org.junit.Assert.assertTrue;
//
//public class OdoCliProjectTest extends OdoCliTest {
//
//
//    @Test
//    public void checkCreateProject() throws IOException {
//        String project = PROJECT_PREFIX + random.nextInt();
//        try {
//            createProject(project);
//        } finally {
//            odo.deleteProject(project);
//        }
//    }
//
//    @Test
//    public void checkListProjects() throws IOException {
//        String project = PROJECT_PREFIX + random.nextInt();
//        try {
//            createProject(project);
//            List<String> projects = odo.getNamespaces();
//            assertTrue(projects.size() > 0);
//        } finally {
//            odo.deleteProject(project);
//        }
//    }
//}
