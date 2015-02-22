package com.thetransactioncompany.cors.dynamic;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import junit.framework.TestCase;

import com.thetransactioncompany.cors.CORSConfigurationLoader;
import com.thetransactioncompany.cors.MockFilterConfig;


/**
 * Tests the CORS configuration file watcher.
 */
public class CORSConfigurationFileWatcherTest extends TestCase {


	@Override
	public void setUp()
		throws Exception {

		Properties properties = new Properties();
		properties.setProperty("cors.allowOrigin", "https://www.example.org");

		OutputStream os = new FileOutputStream(new File("test.properties"));
		properties.store(os, null);
	}


	public void testWatchParameterName() {

		assertEquals("cors.configFileWatchInterval", CORSConfigurationFileWatcher.WATCH_INTERVAL_PARAM_NAME);
	}


	public void testDefaultWatchIntervalConstant() {

		assertEquals(20, CORSConfigurationFileWatcher.DEFAULT_WATCH_INTERVAL_SECONDS);
	}


	public void testDefaultWatchInterval() {

		MockFilterConfig filterConfig = new MockFilterConfig();
		filterConfig.setInitParameter(CORSConfigurationLoader.CONFIG_FILE_PARAM_NAME, "test.properties");

		CORSConfigurationFileWatcher watcher = new CORSConfigurationFileWatcher(filterConfig);

		assertEquals(CORSConfigurationFileWatcher.DEFAULT_WATCH_INTERVAL_SECONDS, watcher.getWatchIntervalSeconds());
	}


	public void testDetectChange()
		throws Exception {

		// Override watch interval
		System.setProperty(CORSConfigurationFileWatcher.WATCH_INTERVAL_PARAM_NAME, "1");

		MockFilterConfig filterConfig = new MockFilterConfig();
		filterConfig.setInitParameter(CORSConfigurationLoader.CONFIG_FILE_PARAM_NAME, "test.properties");

		CORSConfigurationFileWatcher watcher = new CORSConfigurationFileWatcher(filterConfig);

		assertEquals(1l, watcher.getWatchIntervalSeconds());

		watcher.start();

		Properties properties = new Properties();
		properties.setProperty("cors.allowOrigin", "https://www.example.com");

		OutputStream os = new FileOutputStream(new File("test.properties"));
		properties.store(os, null);

		Thread.sleep(1100);

		assertTrue(watcher.reloadRequired());

		watcher.stop();
	}


	public void testDetectNoChange()
		throws Exception {

		// Override watch interval
		System.setProperty(CORSConfigurationFileWatcher.WATCH_INTERVAL_PARAM_NAME, "1");

		MockFilterConfig filterConfig = new MockFilterConfig();
		filterConfig.setInitParameter(CORSConfigurationLoader.CONFIG_FILE_PARAM_NAME, "test.properties");

		CORSConfigurationFileWatcher watcher = new CORSConfigurationFileWatcher(filterConfig);

		assertEquals(1l, watcher.getWatchIntervalSeconds());

		watcher.start();

		Thread.sleep(1100);

		assertFalse(watcher.reloadRequired());

		watcher.stop();
	}


	@Override
	public void tearDown()
		throws Exception {

		System.clearProperty("cors.configFileWatchInterval");

		new File("test.properties").delete();
	}
}
