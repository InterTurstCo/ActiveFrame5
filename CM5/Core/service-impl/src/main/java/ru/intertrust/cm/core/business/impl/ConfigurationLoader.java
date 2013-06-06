package ru.intertrust.cm.core.business.impl;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.CollectionConfiguration;
import ru.intertrust.cm.core.config.Configuration;

import java.io.InputStream;

/**
 * Класс, предназначенный для загрузки конфигурации бизнес-объектов
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class ConfigurationLoader {

    private String configurationFilePath;

    private String collectionsConfigurationFilePath;
    
    private ConfigurationService configurationService;

    private Configuration configuration;

    private CollectionConfiguration collectionConfiguration;
    
    private ConfigurationValidator configurationValidator;

    public ConfigurationLoader() {
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

    /**
     * Устанавливает {@link #configurationService}
     * @param configurationService сервис для работы с конфигурацией бизнес-объектов
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Возвращает загруженную конфигурацию бизнес-объектов
     * @return конфигурация
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    
    /**
     * Возвращает загруженную конфигурацию коллекций
     * @return
     */
    public CollectionConfiguration getCollectionConfiguration() {
        return collectionConfiguration;
    }
   
    /**
     * Инициализирует и возвращает {@link #configurationValidator}
     * @return
     */
    public ConfigurationValidator getConfigurationValidator() {
        configurationValidator.setConfigurationPath(configurationFilePath);
        configurationValidator.setCollectionsConfigurationPath(collectionsConfigurationFilePath);        
        configurationValidator.setConfiguration(configuration);
        return configurationValidator;
    }

    /**
     * Устанавливает {@link #configurationValidator}
     * @param configurationValidator валидатор конфигурации бизнас-объектов
     */
    public void setConfigurationValidator(ConfigurationValidator configurationValidator) {
        this.configurationValidator = configurationValidator;
    }


    /**
     * Загружает конфигурацию бизнес-объектов, валидирует и создает соответствующие сущности в базе.
     * Добавляет запись администратора (admin/admin) в таблицу authentication_info.
     * @throws Exception
     */
    public void load() throws Exception {
        configuration = serializeBusinessObjectsConfiguration(configurationFilePath);
        collectionConfiguration = serializeCollectionConfiguration(collectionsConfigurationFilePath);
        
        validateConfiguration();

        configurationService.loadConfiguration(configuration);

    }

    /**
     * Сериализация конфигурации в Java класс.
     * @param configurationFilePath путь к конфигурационному файлу
     * @return {@link Configuration}
     * @throws Exception
     */
    private <T> T serializeConfiguration(String configurationFilePath, Class<T> configurationClass) throws Exception {
        Serializer serializer = new Persister();
        InputStream source = getResourceAsStream(configurationFilePath);
        return serializer.read(configurationClass, source);
    }

    protected Configuration serializeBusinessObjectsConfiguration(String configurationFilePath) throws Exception {
        return serializeConfiguration(configurationFilePath, Configuration.class);
    }

    protected CollectionConfiguration serializeCollectionConfiguration(String configurationFilePath) throws Exception {
        return serializeConfiguration(configurationFilePath, CollectionConfiguration.class);
    }
    
    
    private InputStream getResourceAsStream(String resourcePath) {
        return FileUtils.getFileInputStream(resourcePath);
    }

    private void validateConfiguration() {
        getConfigurationValidator().validate();
    }

}
