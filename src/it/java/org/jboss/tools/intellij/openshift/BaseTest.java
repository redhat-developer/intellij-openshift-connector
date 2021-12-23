/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift;

import com.intellij.openapi.project.Project;
import com.intellij.remoterobot.RemoteRobot;



import com.redhat.devtools.intellij.commonUiTestLibrary.UITestRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class BaseTest {
    private static RemoteRobot robot;

    protected Project project;

    @BeforeAll
    public static void connect() {
        robot = UITestRunner.runIde(UITestRunner.IdeaVersion.V_2020_2, 8580);
    }

    @Test
    public void iokhrimeTest() throws InterruptedException {
        TimeUnit.SECONDS.sleep(60);
    }

    @AfterAll
    public static void closeIde() {
        UITestRunner.closeIde();
    }
}
