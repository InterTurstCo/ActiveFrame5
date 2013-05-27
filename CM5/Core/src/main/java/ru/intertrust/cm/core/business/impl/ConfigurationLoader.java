package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;
import ru.intertrust.cm.core.config.Configuration;

/**
 * Класс, предназначенный для загрузки конфигурации бизнес-оъектов
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class ConfigurationLoader {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private String configurationFilePath;
    private ConfigurationService configurationService;

    private Configuration configuration;

    private ConfigurationValidator configurationValidator;

    private AuthenticationService authenticationService;

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
     * Устанавливает сервис аутентификации
     * @param authenticationService AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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

        insertAdminAuthenticationInfoIfEmpty();

    }

    /** 
     * Сериализация конфигурации в Java класс. Нужен для тестовых целей только.
     * @param configurationFilePath путь к конфигурационному файлу
     * @return {@link Configuration}
     * @throws Exception
     */
    public Configuration serializeConfiguration(String configurationFilePath) throws Exception {
        Serializer serializer = new Persister();
        InputStream source = getResourceAsStream(configurationFilePath);
        return serializer.read(Configuration.class, source);
    }

    /**
     * Метод нужен для тестовых целей. Так как способ загрузки ресусров отличается для тестовых классов и основных (продакшен) классов.
     * @param resourcePath относительный путь к ресурсу
     * @return
     */
    protected InputStream getResourceAsStream(String resourcePath) {
        return FileUtils.getFileInputStream(resourcePath);
    }
    
    private void validateConfiguration() {
        getConfigurationValidator().validate();
    }

    /**
     * Добавляет запись для Администратора в таблицу пользователей, если такой записи еще не существует.
     */
    private void insertAdminAuthenticationInfoIfEmpty() {
        if (!authenticationService.existsAuthenticationInfo(ADMIN_LOGIN)) {
            insertAdminAuthenticationInfo();
        }
    }

    private void insertAdminAuthenticationInfo() {
        AuthenticationInfo admin = new AuthenticationInfo();
        admin.setId(1);
        admin.setUserUid(ADMIN_LOGIN);
        admin.setPassword(ADMIN_PASSWORD);
        authenticationService.insertAuthenticationInfo(admin);
    }
}
