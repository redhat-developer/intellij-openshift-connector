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
package org.jboss.tools.intellij.openshift.utils;

import io.fabric8.kubernetes.api.model.Config;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class ConfigWatcher implements Runnable {
    private final File config;
    private final Listener listener;

    public interface Listener {
        void onUpdate(ConfigWatcher source, Config config);
    }

    public ConfigWatcher(File config, Listener listener) {
        this.config = config;
        this.listener = listener;
    }

    @Override
    public void run() {
        runOnConfigChange(() -> {
            try {
                if (!ConfigHelper.isKubeConfigParsable()) {
                    return;
                }
                Config config = ConfigHelper.loadKubeConfig();
                listener.onUpdate(this, config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void runOnConfigChange(final Runnable runnable) {
        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            registerWatchService(service);
            WatchKey key;
            while ((key = service.take()) != null) {
                key.pollEvents().stream()
                        .filter(event -> {
                            Path path = getPath(event);
                            return path.equals(config.toPath());
                        })
                        .forEach((Void) -> runnable.run());
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @NotNull
    private void registerWatchService(WatchService service) throws IOException {
        HighSensitivityRegistrar modifier = new HighSensitivityRegistrar();
        modifier.registerService(config.getParentFile().toPath(),
                new WatchEvent.Kind[]{
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE},
                service);
    }

    @NotNull
    private Path getPath(WatchEvent<?> event) {
        Path path = (Path) event.context();
        return config.getParentFile().toPath().resolve(path);
    }
}
