package com.openclawlite.adapter.plugin.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Class loader for plugin isolation
 * Each plugin gets its own class loader to prevent conflicts
 */
public class PluginClassLoader extends URLClassLoader {
    
    private final String pluginId;
    private final ClassLoader parent;
    private final List<String> sharedPackages;
    
    /**
     * Create a plugin class loader
     * 
     * @param pluginId Plugin ID
     * @param URLs JAR URLs for the plugin
     * @param parent Parent class loader
     * @param sharedPackages Packages to share with parent (e.g., API interfaces)
     */
    public PluginClassLoader(
            String pluginId,
            URL[] URLs,
            ClassLoader parent,
            List<String> sharedPackages) {
        
        super(URLs, parent);
        this.pluginId = pluginId;
        this.parent = parent;
        this.sharedPackages = sharedPackages != null ? sharedPackages : List.of();
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Check if we've already loaded this class
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            
            if (c == null) {
                // For shared packages, use parent class loader
                if (isSharedPackage(name)) {
                    try {
                        c = parent.loadClass(name);
                    } catch (ClassNotFoundException e) {
                        // Fall through to load from plugin
                    }
                }
                
                // Load from plugin
                if (c == null) {
                    try {
                        c = findClass(name);
                    } catch (ClassNotFoundException e) {
                        // Try parent as last resort
                        c = parent.loadClass(name);
                    }
                }
            }
            
            if (resolve) {
                resolveClass(c);
            }
            
            return c;
        }
    }
    
    /**
     * Check if a class is in a shared package
     */
    private boolean isSharedPackage(String className) {
        for (String pkg : sharedPackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (IOException e) {
            // Ignore
        }
    }
    
    public String getPluginId() {
        return pluginId;
    }
}
