/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.utils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

/**
 * @author Martin Szuc
 * Utility class used for manipulation for .kube/config file
 */
public class KubeConfigUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(KubeConfigUtility.class);
    private static final String USER_HOME = System.getProperty("user.home");
    private static final Path CONFIG_FILE_PATH = getKubeConfigPath();
    private static final Path BACKUP_FILE_PATH = Paths.get(USER_HOME, ".kube", "config.bak");

    public static void removeKubeConfig() {
        try {
            LOGGER.info("Attempting to delete kube config file");
            Files.deleteIfExists(CONFIG_FILE_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to delete kube config file: {}", e.getMessage());
        }
    }

    public static void backupKubeConfig() {
        try {
            LOGGER.info("Attempting to backup kube config file");
            Files.copy(CONFIG_FILE_PATH, BACKUP_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (NoSuchFileException e) {
            LOGGER.info("Kube config file not present, no need to create backup");
        } catch (IOException e) {
            LOGGER.error("Failed to backup kube config file: {}", e.getMessage());
        }
    }

    public static void restoreKubeConfig() {
        try {
            LOGGER.info("Attempting to restore kube config file");
            Files.copy(BACKUP_FILE_PATH, CONFIG_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (NoSuchFileException e) {
            LOGGER.info("Backup file not present, no need to restore");
        } catch (IOException e) {
            LOGGER.error("Failed to restore kube config file: {}", e.getMessage());
        }
    }


    @NotNull
    public static Path getKubeConfigPath() {
        Path configFilePath;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            configFilePath = Paths.get(System.getenv("USERPROFILE"), ".kube", "config");
        } else if (os.contains("mac")) {
            configFilePath = Paths.get(System.getProperty("user.home"), ".kube", "config");
        } else {
            configFilePath = Paths.get(USER_HOME, ".kube", "config");
        }
        return configFilePath;
    }
}
