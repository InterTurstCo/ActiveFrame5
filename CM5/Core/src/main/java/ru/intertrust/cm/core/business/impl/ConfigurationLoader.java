package ru.intertrust.cm.core.business.impl;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.Person;
import ru.intertrust.cm.core.config.Configuration;

import java.io.InputStream;
import java.util.Date;

/**
 * @author vmatsukevich
 *         Date: 5/6/13
 *         Time: 9:36 AM
 */
public class ConfigurationLoader {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_EMAIL = "admin@intertrust.ru";


    private String configurationFilePath;
    private ConfigurationService configurationService;

    private Configuration configuration;

    private ConfigurationValidator configurationValidator;

    private PersonService personService;

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

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @throws Exception
     */
    public void load() throws Exception {
        Serializer serializer = new Persister();
        InputStream source = FileUtils.getFileInputStream(configurationFilePath);
        configuration = serializer.read(Configuration.class, source);

        validateConfiguration();

        configurationService.loadConfiguration(configuration);

        insertAdminPersonIfEmpty();
    }

    private void validateConfiguration() {
        getConfigurationValidator().validate();
    }

    /**
     * Добавляет запись для Администратора в таблицу пользователей, если такой записи еще не существует.
     */
    private void insertAdminPersonIfEmpty() {
        if (!personService.existsPerson(ADMIN_LOGIN)) {
            insertAdminPerson();
        }
    }

    private void insertAdminPerson() {
        Person admin = new Person();
        admin.getConfiguredFields().put("id", 1);
        admin.getConfiguredFields().put("login", ADMIN_LOGIN);
        admin.getConfiguredFields().put("password", ADMIN_PASSWORD);
        admin.getConfiguredFields().put("email", ADMIN_EMAIL);
        admin.getConfiguredFields().put("created_date", new Date());
        admin.getConfiguredFields().put("updated_date", new Date());
        personService.insertPerson(admin);
    }

}
