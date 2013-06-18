package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.DomainObjectConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDAO;

import java.util.Collection;
import java.util.HashSet;
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

    @Autowired
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
     * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationService#loadConfiguration()}
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
        private final Set<String> loadedDomainObjectConfigs = new HashSet<>();

        private RecursiveLoader() {
        }

        private void load() {
            Collection<DomainObjectConfig> configList = configurationExplorer.getDomainObjectConfigs();
            if(configList.isEmpty())  {
                return;
            }

            dataStructureDAO.createServiceTables();

            for(DomainObjectConfig config : configList) {
                loadDomainObjectConfig(config);
            }
        }

        private void loadDomainObjectConfig(DomainObjectConfig domainObjectConfig) {
            if(loadedDomainObjectConfigs.contains(domainObjectConfig.getName())) { // skip if already loaded
                return;
            }

            // First load referenced domain object configurations
            loadDependentDomainObjectConfigs(domainObjectConfig);

            dataStructureDAO.createTable(domainObjectConfig);
            dataStructureDAO.createSequence(domainObjectConfig);
            loadedDomainObjectConfigs.add(domainObjectConfig.getName()); // add to loaded configs set
        }

        private void loadDependentDomainObjectConfigs(DomainObjectConfig domainObjectConfig) {
            for(FieldConfig fieldConfig : domainObjectConfig.getFieldConfigs()) {
                if((ReferenceFieldConfig.class.equals(fieldConfig.getClass()))) {
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    loadDomainObjectConfig(configurationExplorer.getDomainObjectConfig(referenceFieldConfig.getType()));
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
        AuthenticationInfoAndRole admin = new AuthenticationInfoAndRole();
        admin.setUserUid(ADMIN_LOGIN);
        admin.setPassword(ADMIN_PASSWORD);
        admin.setRole("admin");
        authenticationService.insertAuthenticationInfoAndRole(admin);
    }

}
