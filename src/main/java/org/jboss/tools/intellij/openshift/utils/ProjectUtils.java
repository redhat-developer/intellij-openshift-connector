/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import java.io.File;
import java.nio.file.Files;

public class ProjectUtils {

    private ProjectUtils() {
        // prevent instantiation
    }

    public static boolean isEmpty(File project) {
        if (Files.exists(project.toPath())) {
            for (File f : project.listFiles()) {
                if (!f.getName().startsWith(".")) {
                    return false;
                }
            }
        }
        return true;
    }
}
