package com.thetransactioncompany.cors.autoreconf;


import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.thetransactioncompany.cors.CORSConfigurationLoader;
import com.thetransactioncompany.cors.environment.Environment;
import com.thetransactioncompany.cors.environment.SystemProperties;


/**
 * Watches a CORS filter configuration file for changes.
 *
 * <p>The file system is polled every {@link #DEFAULT_POLL_INTERVAL_SECONDS
 * 20 seconds}. This can be overridden by setting a
 * {@code cors.configFilePollInterval} system property to the desired value
 * (in seconds).
 *
 * @author Aleksey Zvolinsky
 */
public class CORSConfigurationFileWatcher implements CORSConfigurationWatcher {


	/**
	 * The system property name for the configuration file poll interval,
	 * in seconds.
	 */
	public static final String POLL_INTERVAL_PARAM_NAME = "cors.configFilePollInterval";


	/**
	 * The default poll interval (and initial poll delay), in seconds.
	 */
	public static final long DEFAULT_POLL_INTERVAL_SECONDS = 20;


	/**
	 * The logger.
	 */
	private static final Logger LOG = LogManager.getLogManager().getLogger("");


	/**
	 * The polling scheduler.
	 */
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


	/**
	 * The file system watch service.
	 */
	private WatchService watcher;


	/**
	 * The servlet filter configuration.
	 */
	private final FilterConfig filterConfig;


	/**
	 * The environment properties.
	 */
	private Environment environment;


	/**
	 * The configuration file name.
	 */
	private String configFile;


	/**
	 * Indicates whether the configuration file has changed and the filter
	 * must be reloaded.
	 */
	private boolean reloadRequired;


	/**
	 * Creates a new CORS filter configuration watcher.
	 *
	 * @param filterConfig The filter configuration. Must not be
	 *                     {@code null}.
	 */
	public CORSConfigurationFileWatcher(final FilterConfig filterConfig) {

		if (filterConfig == null) {
			throw new IllegalArgumentException("The servlet filter configuration must not be null");
		}

		this.filterConfig = filterConfig;

		try {
			watcher = FileSystems.getDefault().newWatchService();
			Path dir = determineConfigDir();
			dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
			LOG.fine("CORS Filter: Started watching for configuration file changes within " + dir);
		} catch (IOException e) {
			LOG.severe("CORS Filter: Failed to initialize file system watcher: " + e.getMessage());
		}
	}


	@Override
	public void start() {

		scheduler.scheduleAtFixedRate(
			new Runnable() {
				@Override
				public void run() {
					LOG.finest("CORS Filter: Initiated configuration file poll");
					try {
						pollConfigFileForChanges();

					} catch (Throwable throwable) {
						LOG.severe("CORS Filter: Configuration file polling failed: " + throwable);
					}
				}
			},
			getPollIntervalSeconds(), // initial watch delay
			getPollIntervalSeconds(), // watch interval
			TimeUnit.SECONDS);
	}


	@Override
	public boolean reloadRequired() {

		return reloadRequired;
	}


	@Override
	public void stop() {

		scheduler.shutdown();
	}


	@Override
	public void reset() {

		reloadRequired = false;
	}


	/**
	 * Polls the configuration file for changes.
	 */
	private void pollConfigFileForChanges() {

		WatchKey key = watcher.poll();
		try {
			if (key == null) {
				return;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				Kind<?> kind = event.kind();
				if (StandardWatchEventKinds.OVERFLOW == kind) {
					continue;
				}

				// The filename is the context of the event.
				Path filename = (Path) event.context();

				if (configFile.endsWith(filename.toString())) {
					LOG.info("CORS Filter: Detected change in " + configFile + " , configuration reload required");
					reloadRequired = true;
				}
			}
		} finally {
			if (key != null) {
				key.reset();
			}
		}
	}


	/**
	 * Determines the CORS configuration file directory.
	 *
	 * @see CORSConfigurationLoader#load()
	 *
	 * @return The path to the directory where the configuration file is
	 *         located.
	 */
	private Path determineConfigDir() {
		try {
			// Try to get the config file from the sys environment
			configFile = getEnvironment().getProperty(CORSConfigurationLoader.CONFIG_FILE_PARAM_NAME);

			if (configFile == null || configFile.trim().isEmpty()) {
				// Try to get the config file from the filter init param
				configFile = filterConfig.getInitParameter(CORSConfigurationLoader.CONFIG_FILE_PARAM_NAME);
			}

			if (configFile == null) {
				throw new RuntimeException("CORS configuration file is not defined");
			}

			URL url = filterConfig.getServletContext().getResource(configFile);

			if (url == null) {
				final String msg = "CORS Filter: Configuration file not found: " + configFile;
				LOG.severe(msg);
				throw new RuntimeException(msg);
			}

			return Paths.get(url.toURI()).getParent();

		} catch (Exception e) {

			throw new RuntimeException(e.getMessage(), e);
		}
	}


	/**
	 * Gets the current system variables environment (using lazy loading).
	 *
	 * @return The system variables environment.
	 */
	private Environment getEnvironment() {
		if (environment == null) {
			environment = new SystemProperties();
		}

		return environment;
	}


	/**
	 * Gets the configured poll interval.
	 *
	 * @return The poll interval, in seconds.
	 */
	public long getPollIntervalSeconds() {

		String period = getEnvironment().getProperty(POLL_INTERVAL_PARAM_NAME);

		if (period == null) {
			LOG.fine("CORS Filter: Defaulted configuration file poll period to " + DEFAULT_POLL_INTERVAL_SECONDS + " seconds, " +
				"may be overridden with " + POLL_INTERVAL_PARAM_NAME + " system property");
			return DEFAULT_POLL_INTERVAL_SECONDS;
		}

		long overriddenPeriod = Long.parseLong(period);
		LOG.fine("CORS Filter: Set configuration file poll period to " + overriddenPeriod + " seconds from " + POLL_INTERVAL_PARAM_NAME + " system property");
		return overriddenPeriod;
	}
}