package com.thetransactioncompany.cors.autoreconf;


import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.thetransactioncompany.cors.CORSConfigurationException;
import com.thetransactioncompany.cors.CORSConfigurationLoader;
import com.thetransactioncompany.cors.CORSFilter;


/**
 * CORS servlet filter which has the ability to automatically detect changes to
 * the configuration file and reconfigure itself. The configuration file will
 * be checked the next time the filter is invoked and the poll interval has
 * elapsed since the last check.
 */
public class AutoReconfigurableCORSFilter implements Filter {


	/**
	 * Logger.
	 */
	private static final Logger LOG = LogManager.getLogManager().getLogger("");


	/**
	 * The current CORS filter.
	 */
	private volatile CORSFilter filter;


	/**
	 * The configuration file watcher.
	 */
	private volatile CORSConfigurationWatcher watcher;


	/**
	 * For loading the CORS filter configuration.
	 */
	private CORSConfigurationLoader loader;


	@Override
	public void init(final FilterConfig filterConfig)
		throws ServletException {

		loader = new CORSConfigurationLoader(filterConfig);
		watcher = new CORSConfigurationFileWatcher(filterConfig);
		watcher.start();
	}


	/**
	 * Returns the current CORS filter.
	 *
	 * @return The current CORS filter.
	 */
	public CORSFilter getFilter() {

		if (watcher.reloadRequired() || filter == null) {

			synchronized (AutoReconfigurableCORSFilter.class) {

				if (watcher.reloadRequired() || filter == null) {

					try {
						if (filter == null) {
							LOG.info("CORS Filter: Initiated first configuration");
						} else {
							LOG.info("CORS Filter: Initiated re-configuration");
						}

						final Filter oldFilter = filter;
						filter = new CORSFilter(loader.load());
						if (oldFilter != null) {
							oldFilter.destroy();
						}
						watcher.reset();

					} catch (CORSConfigurationException e) {
						LOG.severe("CORS Filter: Failed to instantiate new CORS filter: " + e.getMessage());
					}
				}
			}
		}
		return filter;
	}


	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
		throws IOException, ServletException {

		getFilter().doFilter(request, response, chain);
	}


	@Override
	public void destroy() {

		watcher.stop();

		if (filter != null) {
			filter.destroy();
		}
	}
}
