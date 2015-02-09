package com.thetransactioncompany.cors.dynamic;

/**
 * Configuration changes detection for dynamic configuration reloading.
 * 
 * @author Aleksey Zvolinsky
 *
 */
public interface CORSConfigurationWatcher
{
	/**
	 * Start watcher service
	 */
    void start();
    
    /**
	 * Stop watcher service
	 */
    void stop();
    
    /**
     * 
     * @return true when changes observed
     */
    boolean reloadRequired();
    
    /**
     * reset state of watcher
     */    
    void reset();
}
