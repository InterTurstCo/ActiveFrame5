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

    public String getConfigurationFilePath() {
        return configurationFilePath;
    }

    public void setConfigurationFilePath(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ConfigurationValidator getConfigurationValidator() {
        configurationValidator.setConfigurationPath(configurationFilePath);
        configurationValidator.setConfiguration(configuration);
        return configurationValidator;
    }

    public void setConfigurationValidator(ConfigurationValidator configurationValidator) {
        this.configurationValidator = configurationValidator;
    }

    public ConfigurationLoader(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

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

        createAuthenticationInfoTable();
        configurationService.loadConfiguration(configuration);

        insertAdminauthenticationInfoIfEmpty();
        
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
     * @param person
     */
    private void insertAdminauthenticationInfoIfEmpty() {
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
