package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.config.model.Configuration;

import java.io.StringWriter;
import java.util.Set;

/**
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:36 PM
 */
public class ConfigurationSerializer {

    private String configurationSchemaFilePath;
    private Set<String> configurationFilePaths;

    public ConfigurationSerializer() {
    }

    public static String deserializeConfiguration(Configuration configuration) {
        Serializer serializer = new Persister();
        StringWriter stringWriter = new StringWriter();

        try {
            serializer.write(configuration, stringWriter);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to deserialize configuration");
        }

        return stringWriter.toString();
    }

    public static Configuration serializeTrustedConfiguration(String configurationString) throws
            ConfigurationException {
        try {
            Serializer serializer = new Persister();
            return serializer.read(Configuration.class, configurationString);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to serialize configuration from String", e);
        }
    }

    public void setConfigurationSchemaFilePath(String configurationSchemaFilePath) {
        this.configurationSchemaFilePath = configurationSchemaFilePath;
    }

    public void setConfigurationFilePaths(Set<String> configurationFilePaths) {
        this.configurationFilePaths = configurationFilePaths;
    }

    public Configuration serializeConfiguration() throws Exception {
        if(configurationFilePaths == null || configurationFilePaths.isEmpty()) {
            throw new RuntimeException("Configuration paths aren't specified");
        }

        Configuration combinedConfiguration = new Configuration();

        for(String configurationFilePath : configurationFilePaths) {
            Configuration partialConfiguration = serializeConfiguration(configurationFilePath);
            combineConfigurations(partialConfiguration, combinedConfiguration);
        }

        return combinedConfiguration;
    }

    private Configuration combineConfigurations(Configuration source, Configuration destination) {
        if(destination == null) {
            throw new IllegalArgumentException("Destination Configuration cannot be null");
        }

        if(source != null) {
            destination.getConfigurationList().addAll(source.getConfigurationList());
        }

        return destination;
    }

    /**
     * Сериализация конфигурации в Java класс.
     * @param configurationFilePath путь к файлу конфигурации
     * @return {@link ru.intertrust.cm.core.config.model.Configuration}
     * @throws Exception
     */
    private Configuration serializeConfiguration(String configurationFilePath) throws Exception {
        ConfigurationSchemaValidator schemaValidator = new ConfigurationSchemaValidator(configurationFilePath,
                configurationSchemaFilePath);
        schemaValidator.validate();

        Serializer serializer = new Persister();
        return serializer.read(Configuration.class, FileUtils.getFileInputStream(configurationFilePath));
    }
}
