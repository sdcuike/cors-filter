package com.thetransactioncompany.cors.dynamic;

import java.io.IOException;
import java.net.URISyntaxException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thetransactioncompany.cors.CORSConfigurationLoader;
import com.thetransactioncompany.cors.environment.Environment;
import com.thetransactioncompany.cors.environment.SystemProperties;

/**
 * 
 * <p>Configuration changes detection for dynamic configuration reloading.
 * 
 * <p>The <b>cors.watchPeriodInSeconds</b> JVM system property can be used for configuring watch period.
 * <p>By default it is 20 seconds
 * 
 * 
 * @author Aleksey Zvolinsky
 *
 */
public class CORSConfigurationFileWatcher implements CORSConfigurationWatcher
{
    public static final String WATCHER_PERIOD_PARAM_NAME = "cors.watchPeriodInSeconds";

    private static final long INITIAL_DELAY = 20;
    private static final long DEFAULT_PERIOD = 20;
    private static final Logger LOG = LoggerFactory.getLogger(CORSConfigurationFileWatcher.class);

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private WatchService watcher;

    private final FilterConfig filterConfig;
    private Environment environment;
    private String configFile;
    private boolean reloadRequired;

    public CORSConfigurationFileWatcher(FilterConfig filterConfig)
    {
        if (filterConfig == null)
        {
            throw new IllegalArgumentException("The servlet filter configuration must not be null");
        }

        this.filterConfig = filterConfig;

        try
        {
            watcher = FileSystems.getDefault().newWatchService();
            Path dir = getConfigDir();
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            LOG.debug("Started watching for updates inside {} directory", dir);
        }
        catch (IOException e)
        {
            LOG.error("Failed to initialize file system watcher");
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void start()
    {
        scheduler.scheduleAtFixedRate(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LOG.trace("Start watching for file update");
                        try
                        {
                            processEvent();
                        }
                        catch (Throwable th)
                        {
                            LOG.error("Watching failed: ", th);
                        }
                    }
                }, INITIAL_DELAY, getPeriodSeconds(), TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public boolean reloadRequired()
    {
        return reloadRequired;
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public void stop()
    {
        scheduler.shutdown();
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public void reset()
    {
        reloadRequired = false;
    }

    private void processEvent()
    {
        LOG.trace("Started processing events");
        WatchKey key = watcher.poll();
        try
        {
            if (key == null)
            {
                return;
            }
            for (WatchEvent<?> event : key.pollEvents())
            {
                Kind<?> kind = event.kind();
                if (StandardWatchEventKinds.OVERFLOW == kind)
                {
                    continue;
                }

                //The filename is the context of the event.
                Path filename = (Path) event.context();

                if (filename.endsWith(configFile))
                {
                    LOG.debug("File {} is changed. Reload is required", configFile);
                    reloadRequired = true;
                }
            }
        }
        finally
        {
            if(key != null)
            {
                key.reset();
            }
            LOG.trace("Finished processing events");
        }
    }

    /**
     * @see CORSConfigurationLoader#load()
     * 
     * @return path to configuration
     */
    private Path getConfigDir()
    {
        try
        {
            // Try to get the config file from the sys environment
            configFile = getEnvironment()
                    .getProperty(CORSConfigurationLoader.CONFIG_FILE_PARAM_NAME);

            if (configFile == null || configFile.trim().isEmpty())
            {
                // Try to get the config file from the filter init param
                configFile = filterConfig.getInitParameter(CORSConfigurationLoader.CONFIG_FILE_PARAM_NAME);
            }

            if (configFile == null)
            {
                throw new RuntimeException("CORS configuration file is not defined");
            }

            URL url = Thread.currentThread().getContextClassLoader().getResource(configFile);

            if (url == null)
            {
                LOG.error("File not found: " + configFile);
                throw new RuntimeException("File not found: " + configFile);
            }

            return Paths.get(url.toURI()).getParent();
        }
        catch(URISyntaxException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gets the current system variables environment (lazy loading).
     *
     * @return The system variables environment.
     */
    private Environment getEnvironment()
    {
        if(environment == null)
        {
            environment = new SystemProperties();
        }

        return environment;
    }

    private long getPeriodSeconds()
    {
        String period = filterConfig.getInitParameter(WATCHER_PERIOD_PARAM_NAME);
        if(period == null)
        {
            LOG.debug("Period is not setup by {} variable, default value {} seconds will be used", WATCHER_PERIOD_PARAM_NAME, DEFAULT_PERIOD);
            return DEFAULT_PERIOD;
        }
        long parsedLong = Long.parseLong(period);
        LOG.debug("Defined period is {} seconds", parsedLong);
        return parsedLong;
    }
}