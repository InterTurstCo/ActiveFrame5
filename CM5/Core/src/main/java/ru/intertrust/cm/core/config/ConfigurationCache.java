package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.StringWriter;

/**
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class ConfigurationCache {

    private String configurationFilePath;
    private Configuration configuration;

    public ConfigurationCache() {
    }

    public String getConfigurationFilePath() {
        return configurationFilePath;
    }

    public void setConfigurationFilePath(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public ConfigurationCache(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void init() throws Exception {
        Serializer serializer = new Persister();
        File source = new File(configurationFilePath);
        configuration = serializer.read(Configuration.class, source);
/*        StringWriter stringWriter = new StringWriter();
        serializer.write(example, stringWriter);
        System.out.println(stringWriter.toString());*/
    }
}
