package com.thetransactioncompany.cors;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;


/**
 * Mock servlet filter configuration.
 *
 * @author Vladimir Dzhuvinov
 */
public class MockFilterConfig implements FilterConfig {


	private final Map<String,String> initParameters = new HashMap<String, String>();


	@Override
	public String getFilterName() {

		return "CORSFilter";
	}


	@Override
	public String getInitParameter(final String name) {

		return initParameters.get(name);
	}


	public void setInitParameter(final String name, final String value) {

		initParameters.put(name, value);
	}


	@Override
	public Enumeration getInitParameterNames() {

		return new Vector<String>(initParameters.keySet()).elements();
	}


	@Override
	public ServletContext getServletContext() {

		return new MockServletContext();
	}
}