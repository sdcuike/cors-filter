package com.thetransactioncompany.cors.dynamic;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thetransactioncompany.cors.CORSConfigurationException;
import com.thetransactioncompany.cors.CORSConfigurationLoader;
import com.thetransactioncompany.cors.CORSFilter;

public class DynamicCORSFilter implements Filter
{
    private static final Logger LOG = LoggerFactory.getLogger(DynamicCORSFilter.class);

    private volatile CORSFilter filter;
    private volatile CORSConfigurationWatcher watcher;
    private CORSConfigurationLoader loader;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        loader = new CORSConfigurationLoader(filterConfig);
        watcher = new CORSConfigurationFileWatcher(filterConfig);
        watcher.start();
    }

    public CORSFilter getFilter()
    {
        if(watcher.reloadRequired() || filter == null)
        {
            synchronized (DynamicCORSFilter.class)
            {
                if(watcher.reloadRequired() || filter == null)
                {
                    try
                    {
                        if (filter == null)
                        {
                            LOG.debug("Loading CORS configuration");
                        }
                        else
                        {
                            LOG.debug("Re-loading CORS configuration");
                        }
                        Filter oldFilter = filter;
                        filter = new CORSFilter(loader.load());
                        if (oldFilter != null)
                        {
                            oldFilter.destroy();
                        }
                        watcher.reset();
                    }
                    catch (CORSConfigurationException e)
                    {
                        LOG.error("Failed to instantiate CORS filter");
                    }
                }
            }
        }
        return filter;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        getFilter()
                .doFilter(request, response, chain);
    }

    @Override
    public void destroy()
    {
        if (filter != null)
        {
            filter.destroy();
        }
        watcher.stop();
    }
}
