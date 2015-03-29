package com.thetransactioncompany.cors.demo;


import java.util.logging.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * Logging (JUL) configurator.
 */
public class LoggingConfigurator implements ServletContextListener {


	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		System.setProperty("java.util.logging.ConsoleHandler.level", "ALL");

		Handler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.ALL);
		Logger.getLogger("").addHandler(handler);

		System.out.println("[CORS out] Configured logging (JUL)");
		System.err.println("[CORS err] Configured logging (JUL)");

		Logger.getLogger("").info("Test info log");
	}


	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {

	}
}
