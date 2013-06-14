package ru.intertrust.cm.core.business.impl;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.config.CollectionConfiguration;
import ru.intertrust.cm.core.config.Configuration;

import java.io.InputStream;

/**
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:36 PM
 */
public class ConfigurationSerializer {

    private String configurationFilePath;
    private String collectionsConfigurationFilePath;
    private String configurationSchemaFilePath;

    public ConfigurationSerializer() {
    }

    /**
     * Устанавливает {@link #configurationFilePath}
     * @param configurationFilePath путь к файлу конфигурации бизнес-объектов
     */
    public void setConfigurationFilePath(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    /**
     * Устанавливает {@link #collectionsConfigurationFilePath}
     * @param collectionsConfigurationFilePath путь к файлу конфигурации коллекций
     */
    public void setCollectionsConfigurationFilePath(String collectionsConfigurationFilePath) {
        this.collectionsConfigurationFilePath = collectionsConfigurationFilePath;
    }

    public void setConfigurationSchemaFilePath(String configurationSchemaFilePath) {
        this.configurationSchemaFilePath = configurationSchemaFilePath;
    }

    public Configuration serializeBusinessObjectConfiguration() throws Exception {
        return serializeConfiguration(configurationFilePath, Configuration.class);
    }

    public CollectionConfiguration serializeCollectionConfiguration() throws Exception {
        return serializeConfiguration(collectionsConfigurationFilePath, CollectionConfiguration.class);
    }

    /**
     * Сериализация конфигурации в Java класс.
     * @param configurationFilePath путь к конфигурационному файлу
     * @return {@link Configuration}
     * @throws Exception
     */
    private <T> T serializeConfiguration(String configurationFilePath, Class<T> configurationClass) throws Exception {
        ConfigurationSchemaValidator schemaValidator = new ConfigurationSchemaValidator(configurationFilePath, configurationSchemaFilePath);
        schemaValidator.validate();

        Serializer serializer = new Persister();
        InputStream source = FileUtils.getFileInputStream(configurationFilePath);
        return serializer.read(configurationClass, source);
    }
}
