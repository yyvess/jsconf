package org.jsconf.core.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.regex.Pattern;

import org.jsconf.core.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigWatchService {

	private static final Pattern WIN_PATH = Pattern.compile("^[/]\\w[:][/].*");
	private static final String[] CONFIG_EXTENTIONS = { "", ".json", ".conf" };
	private final ConfigurationFactory configurationFactory;

	public ConfigWatchService(ConfigurationFactory configurationFactory) {
		this.configurationFactory = configurationFactory;
	}

	public void watch(String ressource) {
		Path path = getConfigurationPath(ressource);
		if (path != null) {
			new Thread(new WatchTask(configurationFactory, path), "Configuration Watch Service").start();
		}
	}

	private Path getConfigurationPath(String ressource) {
		URL systemResource = null;
		for (String extention : CONFIG_EXTENTIONS) {
			if (systemResource == null) {
				systemResource = ClassLoader.getSystemResource(ressource.concat(extention));
			}
		}
		if (systemResource == null) {
			return null;
		}
		String path = systemResource.getFile();
		// Bug on toPath() ?
		if (WIN_PATH.matcher(path).matches()) {
			path = path.substring(1);
		}
		return new File(path).getParentFile().toPath();
	}

	private static final class WatchTask implements Runnable {

		private final Logger log = LoggerFactory.getLogger(this.getClass());
		private ConfigurationFactory configuration;
		private Path path;

		private WatchTask(ConfigurationFactory configuration, Path path) {
			this.configuration = configuration;
			this.path = path;
		}

		public void run() {
			try {
				try (WatchService watchService = path.getFileSystem().newWatchService()) {
					path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
					log.debug("Watch configuration change on {}", path.toString());
					WatchKey watchKey;
					do {
						watchKey = watchService.take();
						for (final WatchEvent<?> event : watchKey.pollEvents()) {
							Kind<?> kind = event.kind();
							if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)
									|| StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
								log.debug("Reloading configuration");
								this.configuration.reload();
								Thread.sleep(10000);
							}
						}
					} while (watchKey.reset());
					log.info("Configuration watching service stoped");
					watchKey.cancel();
					watchService.close();
				}
			} catch (InterruptedException | IOException e) {
				log.error("Configuration watching service stoped", e);
			}
		}
	}
}
