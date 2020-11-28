package net.jmob.jsconf.core.service;

import net.jmob.jsconf.core.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchResource {

    private static final String[] CONFIG_EXTENSIONS = {"", ".json", ".conf"};
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ConfigurationFactory configurationFactory;
    private Thread thread;

    public WatchResource(ConfigurationFactory configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    public WatchResource watch(String resource) {
        Path path = getConfigurationPath(resource);
        if (path != null) {
            this.thread = new Thread(new WatchTask(this.configurationFactory, path), "Configuration watching Service");
            this.thread.start();
        }
        return this;
    }

    public void stop() {
        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
        }
    }

    private Path getConfigurationPath(String resource) {
        URL systemResource = null;
        for (String extension : CONFIG_EXTENSIONS) {
            if (systemResource == null) {
                systemResource = ClassLoader.getSystemResource(resource.concat(extension));
            }
        }
        if (systemResource == null) {
            return null;
        }
        try {
            return Paths.get(systemResource.toURI()).getParent();
        } catch (URISyntaxException e) {
            log.error("Unable to get the path of resource", e);
            return null;
        }
    }

    private static final class WatchTask implements Runnable {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        private final ConfigurationFactory configuration;
        private final Path path;

        private WatchTask(ConfigurationFactory configuration, Path path) {
            this.configuration = configuration;
            this.path = path;
        }

        @Override
        public void run() {
            try (WatchService watchService = this.path.getFileSystem().newWatchService()) {
                this.path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                this.log.debug("Watch configuration change on {}", this.path);
                WatchKey watchKey;
                do {
                    watchKey = watchService.take();
                    for (final WatchEvent<?> event : watchKey.pollEvents()) {
                        Kind<?> kind = event.kind();
                        if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)
                                || StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                            this.log.debug("Reloading configuration");
                            this.configuration.reload();
                            return;
                        }
                    }
                } while (watchKey.reset());
                this.log.info("Configuration watching service stopped");
                watchKey.cancel();
            } catch (IOException | InterruptedException e) {
                this.log.error("Configuration watching service stopped", e);
            }
        }
    }
}
