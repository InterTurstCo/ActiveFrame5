package ru.intertrust.cm.core.business.impl;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.BusinessObjectFieldsConfig;
import ru.intertrust.cm.core.config.Configuration;
import ru.intertrust.cm.core.config.PasswordFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;

import java.io.InputStream;

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
     * Устанавливает сервис аутенфикации
     * @param authenticationService AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * @throws Exception
     */
    public void load() throws Exception {
        Serializer serializer = new Persister();
        InputStream source = FileUtils.getFileInputStream(configurationFilePath);
        configuration = serializer.read(Configuration.class, source);

        validateConfiguration();

        createSystemTables();
        configurationService.loadConfiguration(configuration);

        insertAdminAuthenticationInfoIfEmpty();

    }

    private void createSystemTables() {
        createAuthenticationInfoTable();
    }

    private void createAuthenticationInfoTable() {
        BusinessObjectConfig authenticationInfoConfig = new BusinessObjectConfig();
        authenticationInfoConfig.setName("Authentication Info");
        UniqueKeyConfig uniqueKeyConfig = createAuthenticationInfoUniqueKeys();
        authenticationInfoConfig.getUniqueKeyConfigs().add(uniqueKeyConfig);

        BusinessObjectFieldsConfig businessObjectFieldsConfig = createAuthenticationInfoFields();

        authenticationInfoConfig.setBusinessObjectFieldsConfig(businessObjectFieldsConfig);

        configurationService.loadSystemObjectConfig(authenticationInfoConfig);

    }

    private UniqueKeyConfig createAuthenticationInfoUniqueKeys() {
        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        UniqueKeyFieldConfig uniqueKeyFieldConfig = new UniqueKeyFieldConfig();
        uniqueKeyFieldConfig.setName("user_uid");
        uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig);
        return uniqueKeyConfig;
    }

    private BusinessObjectFieldsConfig createAuthenticationInfoFields() {
        StringFieldConfig userUidField = new StringFieldConfig();
        userUidField.setName("user uid");
        userUidField.setLength(64);
        userUidField.setNotNull(true);

        PasswordFieldConfig passwordField = new PasswordFieldConfig();
        passwordField.setName("password");
        passwordField.setLength(128);
        passwordField.setNotNull(true);

        BusinessObjectFieldsConfig businessObjectFieldsConfig = new BusinessObjectFieldsConfig();
        businessObjectFieldsConfig.getFieldConfigs().add(userUidField);
        businessObjectFieldsConfig.getFieldConfigs().add(passwordField);
        return businessObjectFieldsConfig;
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
