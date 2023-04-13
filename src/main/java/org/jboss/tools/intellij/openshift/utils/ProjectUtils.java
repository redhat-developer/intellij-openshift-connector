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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class ProjectUtils {

    private ProjectUtils() {
        // prevent instantiation
    }

    public static boolean isEmpty(File project) {
        if (Files.exists(project.toPath())) {
            File[] files = project.listFiles();
            if (files != null && files.length > 0) {
                for (File f : Objects.requireNonNull(project.listFiles())) {
                    if (!f.getName().startsWith(".") && !isProjectFile(f.getName())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isProjectFile(String name) {
        return name.endsWith(".iml") || name.endsWith(".ipr") || name.endsWith(".iws");
    }

    public static VirtualFile getDefaultDirectory(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for(Module module : modules) {
            return getModuleRoot(module);
        }
        return null;
    }

    public static VirtualFile getModuleRoot(Module module) {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        VirtualFile[] roots = manager.getContentRoots();
        if (roots.length > 0) {
            return roots[0];
        } else {
            return LocalFileSystem.getInstance().findFileByPath(Objects.requireNonNull(ProjectUtil.guessModuleDir(module)).getPath());
        }
    }
}
