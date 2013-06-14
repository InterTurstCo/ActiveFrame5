package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfo;
import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDAO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationService}
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private ConfigurationExplorer configurationExplorer;

    private DataStructureDAO dataStructureDAO;
    private AuthenticationService authenticationService;

    /**
     * Устанавливает  {@link #dataStructureDAO}
     * @param dataStructureDAO DataStructureDAO
     */
    public void setDataStructureDAO(DataStructureDAO dataStructureDAO) {
        this.dataStructureDAO = dataStructureDAO;
    }

    /**
     * Устанавливает сервис аутентификации
     * @param authenticationService AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationService#loadConfiguration(ru.intertrust.cm.core.config.BusinessObjectsConfiguration)}
     */
    @Override
    public void loadConfiguration() {
        if(isConfigurationLoaded()) {
            return;
        }

        RecursiveLoader recursiveLoader = new RecursiveLoader();
        recursiveLoader.load();

        insertAdminAuthenticationInfoIfEmpty();
    }

    private Boolean isConfigurationLoaded() {
        Integer tablesCount = dataStructureDAO.countTables();
        if(tablesCount == null) {
            throw new RuntimeException("Error occurred when calling DataStructureDAO for tables count");
        }

        return tablesCount > 0;
    }

    private class RecursiveLoader {
        private final Set<String> loadedBusinessObjectConfigs = new HashSet<>();

        private RecursiveLoader() {
        }

        private void load() {
            List<BusinessObjectConfig> businessObjectConfigs = configurationExplorer.getBusinessObjectsConfiguration().getBusinessObjectConfigs();
            if(businessObjectConfigs.isEmpty())  {
                return;
            }

            dataStructureDAO.createServiceTables();

            for(BusinessObjectConfig businessObjectConfig : businessObjectConfigs) {
                loadBusinessObjectConfig(businessObjectConfig);
            }
        }

        private void loadBusinessObjectConfig(BusinessObjectConfig businessObjectConfig) {
            if(loadedBusinessObjectConfigs.contains(businessObjectConfig.getName())) { // skip if already loaded
                return;
            }

            // First load referenced business object configurations
            loadDependentBusinessObjectConfigs(businessObjectConfig);

            dataStructureDAO.createTable(businessObjectConfig);
            dataStructureDAO.createSequence(businessObjectConfig);
            loadedBusinessObjectConfigs.add(businessObjectConfig.getName()); // add to loaded configs set
        }

        private void loadDependentBusinessObjectConfigs(BusinessObjectConfig businessObjectConfig) {
            for(FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
                if((ReferenceFieldConfig.class.equals(fieldConfig.getClass()))) {
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    loadBusinessObjectConfig(configurationExplorer.getBusinessObjectConfig(referenceFieldConfig.getType()));
                }
            }
        }

    }

    /**
     * Добавляет запись для Администратора в таблицу пользователей, если эта запись еще не была добавлена.
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
