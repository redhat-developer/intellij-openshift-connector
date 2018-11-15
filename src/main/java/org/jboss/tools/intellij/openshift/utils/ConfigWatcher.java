package org.jboss.tools.intellij.openshift.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class ConfigWatcher implements Runnable {
    private final File config;
    private final Listener listener;

    public interface Listener {
        void onUpdate(ConfigWatcher source);
    }

    public ConfigWatcher(File config, Listener listener) {
        this.config = config;
        this.listener = listener;
    }

    @Override
    public void run() {
        WatchKey key;

        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            config.getParentFile().toPath().register(service, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            while ((key = service.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path path = (Path) event.context();
                    if (path.getFileName().toString().equals("config")) {
                        listener.onUpdate(this);
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
