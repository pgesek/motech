package org.motechproject.server.config;

import org.apache.commons.io.IOUtils;
import org.motechproject.commons.api.MotechException;
import org.motechproject.config.core.MotechConfigurationException;
import org.motechproject.config.service.ConfigurationService;
import org.motechproject.server.config.domain.MotechSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * SettingsFacade provides an interface to access application configuration present in files or database
 */
public class SettingsFacade {

    private static Logger logger = LoggerFactory.getLogger(SettingsFacade.class);

    private ConfigurationService configurationService;

    private boolean rawConfigRegistered;
    private boolean propsRegistered;

    private Map<String, Properties> config = new HashMap<>();
    private Map<String, Resource> rawConfig = new HashMap<>();
    private Map<String, Properties> defaultConfig = new HashMap<>();

    private Bundle bundle;

    public String getBundleSymbolicName() {
        return bundle != null ? bundle.getSymbolicName() : "";
    }

    public String getBundleVersion() {
        return (bundle != null && bundle.getVersion() != null) ? bundle.getVersion().toString() : "";
    }

    @Autowired(required = false)
    public void setBundleContext(BundleContext bundleContext) {
        this.bundle = bundleContext != null ? bundleContext.getBundle() : null;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        if (configurationService == null) {
            logger.warn(getBundleSymbolicName() +
                    ": ConfigurationService reference was not added. SettingsFacade will use the default properties from the classpath");
        }
        try {
            registerConfigurationSettings();
        } catch (MotechConfigurationException ex) {
            rawConfigRegistered = false;
            propsRegistered = false;
            logger.error(ex.getMessage(), ex);
        }
    }

    public void setConfigFiles(List<Resource> resources) {
        for (Resource configFile : resources) {
            InputStream is = null;
            try {
                is = configFile.getInputStream();

                Properties props = new Properties();
                props.load(is);

                config.put(getResourceFileName(configFile), props);
                defaultConfig.put(getResourceFileName(configFile), props);

            } catch (IOException e) {
                throw new MotechException("Cant load config file " + configFile.getFilename(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        registerAllProperties();
    }

    public void setRawConfigFiles(List<Resource> resources) {
        for (Resource resource : resources) {
            rawConfig.put(getResourceFileName(resource), resource);
        }
        registerAllRawConfig();
    }


    public String getProperty(String key) {
        String result = null;
        String filename = findFilename(key);

        if (filename != null) {
            result = getProperty(key, filename);
        }

        return result;
    }

    public String getProperty(String key, String filename) {
        String result = null;
        Properties props = getProperties(filename);
        if (props != null) {
            result = props.getProperty(key);
        }
        return result;
    }

    public Properties getProperties(String filename) {
        if (propsRegistered) {
            try {
                if (configurationService != null) {
                    Properties p = configurationService.getBundleProperties(getBundleSymbolicName(), filename, defaultConfig.get(filename));
                    config.put(filename, p);
                }
            } catch (IOException e) {
                throw new MotechException("Can't read settings", e);
            }
        }

        Properties result = config.get(filename);
        if (result == null) {
            result = defaultConfig.get(filename);
        }
        return (result == null ? new Properties() : result);
    }

    public void setProperty(String key, String value) {
        String filename = findFilename(key);

        if (filename == null) {
            throw new MotechException("No file containing key " + key);
        }

        setProperty(filename, key, value);
    }

    public void saveConfigProperties(String filename, Properties properties) {
        config.put(filename, properties);
        if (propsRegistered) {
            try {
                if (configurationService != null) {
                    configurationService.addOrUpdateProperties(getBundleSymbolicName(), getBundleVersion(), filename,
                            properties, defaultConfig.get(filename));
                }
            } catch (IOException e) {
                throw new MotechException("Can't save settings " + filename, e);
            }

        }
    }

    public void saveRawConfig(String filename, Resource resource) {
        rawConfig.put(filename, resource);
        try {
            InputStream is = resource.getInputStream();
            if (configurationService != null) {
                configurationService.saveRawConfig(getBundleSymbolicName(), getBundleVersion(), filename, is);
            }
        } catch (IOException e) {
            throw new MotechException("Error saving file " + filename, e);
        }
    }

    public InputStream getRawConfig(String filename) {
        InputStream is = null;

        if (rawConfigRegistered) {
            // read from platform
            try {
                is = configurationService.getRawConfig(getBundleSymbolicName(), filename, rawConfig.get(filename));
            } catch (IOException e) {
                throw new MotechException("Error loading file " + filename, e);
            }
        } else {
            // read resource
            Resource resource = rawConfig.get(filename);
            if (resource != null) {
                try {
                    is = resource.getInputStream();
                } catch (IOException e) {
                    throw new MotechException("Error reading raw config", e);
                }
            }
        }

        return is;
    }

    public Properties asProperties() {
        Properties result = new Properties();
        for (Properties p : config.values()) {
            result.putAll(p);
        }
        return result;
    }

    protected void registerAllProperties() {
        if (configurationService != null) {
            for (Map.Entry<String, Properties> entry : config.entrySet()) {
                String filename = entry.getKey();
                Properties props = entry.getValue();

                registerProperties(filename, props);
            }
            propsRegistered = true;
        }
    }

    protected void registerProperties(String filename, Properties properties) {
        try {
            if (configurationService != null &&
                    !configurationService.registersProperties(getBundleSymbolicName(), filename)) {
                configurationService.addOrUpdateProperties(
                        getBundleSymbolicName(), getBundleVersion(), filename, properties, defaultConfig.get(filename));
            } else if (configurationService != null &&
                    configurationService.registersProperties(getBundleSymbolicName(), filename)) {
                configurationService.updatePropertiesAfterReinstallation(getBundleSymbolicName(), getBundleVersion(),
                        filename, defaultConfig.get(filename), properties);
            }

            if (configurationService != null) {
                Properties registeredProps = configurationService.getBundleProperties(
                        getBundleSymbolicName(), filename, defaultConfig.get(filename));
                config.put(filename, registeredProps);
            }
        } catch (IOException e) {
            throw new MotechException("Cant register settings", e);
        }
    }

    public void unregisterProperties(String symbolicName) {
        configurationService.removeAllBundleProperties(symbolicName);
    }

    protected void registerAllRawConfig() {
        if (configurationService != null) {
            for (Map.Entry<String, Resource> entry : rawConfig.entrySet()) {
                String filename = entry.getKey();
                Resource resource = entry.getValue();

                if (!configurationService.rawConfigExists(getBundleSymbolicName(), filename)) {
                    // register new config with the platform
                    try {
                        InputStream is = resource.getInputStream();
                        configurationService.saveRawConfig(getBundleSymbolicName(), getBundleVersion(), filename, is);
                    } catch (IOException e) {
                        throw new MotechException("Can't save raw config " + filename, e);
                    }
                }
            }
            rawConfigRegistered = true;
        }
    }

    protected String findFilename(String key) {
        for (Map.Entry<String, Properties> entry : config.entrySet()) {
            Properties props = entry.getValue();
            String filename = entry.getKey();

            if (props.containsKey(key)) {
                return filename;
            }
        }

        for (Map.Entry<String, Properties> entry : defaultConfig.entrySet()) {
            Properties props = entry.getValue();
            String filename = entry.getKey();

            if (props.containsKey(key)) {
                return filename;
            }
        }
        return null;
    }

    protected static String getResourceFileName(Resource resource) {
        String name = resource.getFilename();

        if (resource instanceof ClassPathResource) {
            name = ((ClassPathResource) resource).getPath();
        } else {
            int colonIndex = name.indexOf(':');
            if (colonIndex >= 0) {
                name = name.substring(colonIndex + 1);
            }
        }

        return name;
    }

    private void registerConfigurationSettings() {
        if (!propsRegistered) {
            registerAllProperties();
        }
        if (!rawConfigRegistered) {
            registerAllRawConfig();
        }
    }


    private void setProperty(String filename, String key, String value) {
        if (!config.containsKey(filename)) {
            config.put(filename, new Properties());
        }

        Properties props = config.get(filename);
        props.put(key, value);
        saveConfigProperties(filename, props);
    }

    public MotechSettings getPlatformSettings() {
        return configurationService.getPlatformSettings();
    }

    public void savePlatformSettings(MotechSettings settings) {
        configurationService.savePlatformSettings(settings);
    }

    public boolean areConfigurationSettingsRegistered() {
        return propsRegistered && rawConfigRegistered;
    }
}
