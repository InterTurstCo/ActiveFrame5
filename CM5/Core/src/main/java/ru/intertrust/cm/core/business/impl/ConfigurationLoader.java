package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.Configuration;

/**
 * Класс, предназначенный для загрузки конфигурации бизнес-оъектов
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class ConfigurationLoader {

    private String configurationFilePath;
    private ConfigurationService configurationService;

    private Configuration configuration;

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
     * Устанавливает {@link #configurationService}
     * @param configurationService сервис для работы с конфигурацией бизнес-объектов
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Инициализирует и возвращает {@link #configurationValidator}
     * @return
     */
    public ConfigurationValidator getConfigurationValidator() {
        configurationValidator.setConfigurationPath(configurationFilePath);
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
        configuration = serializeConfiguration(configurationFilePath);

        validateConfiguration();

        configurationService.loadConfiguration(configuration);

    }

    /** 
     * Сериализация конфигурации в Java класс. 
     * @param configurationFilePath путь к конфигурационному файлу
     * @return {@link Configuration}
     * @throws Exception
     */
    protected Configuration serializeConfiguration(String configurationFilePath) throws Exception {
        Serializer serializer = new Persister();
        InputStream source = getResourceAsStream(configurationFilePath);
        return serializer.read(Configuration.class, source);
    }

    private InputStream getResourceAsStream(String resourcePath) {
        return FileUtils.getFileInputStream(resourcePath);
    }
    
    private void validateConfiguration() {
        getConfigurationValidator().validate();
    }
  
}
