package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AuthenticationService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.AuthenticationInfoAndRole;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationExplorerImpl;
import ru.intertrust.cm.core.config.ConfigurationSerializer;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.dao.api.ConfigurationDao;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.model.FatalException;

import java.util.*;

/**
 * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationService}
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String COMMON_ERROR_MESSAGE = "It's only allowed to add some new configuration " +
            "but not to modify or delete the existing one.";

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private DataStructureDao dataStructureDao;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Устанавливает  {@link #dataStructureDao}
     * @param dataStructureDao DataStructureDao
     */
    public void setDataStructureDao(DataStructureDao dataStructureDao) {
        this.dataStructureDao = dataStructureDao;
    }

    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
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
    public void loadConfiguration() throws ConfigurationException{
        if (!isConfigurationLoaded()) {
            RecursiveLoader recursiveLoader = new RecursiveLoader();
            recursiveLoader.load();

            saveConfiguration();
            insertAdminAuthenticationInfoIfEmpty();
            return;
        }

        String oldConfigurationString = configurationDao.readLastSavedConfiguration();
        if (oldConfigurationString == null) {
            throw new ConfigurationException("Configuration loading aborted: configuration was previously " +
                    "loaded but wasn't saved");
        }

        Configuration oldConfiguration;
        try {
            oldConfiguration = ConfigurationSerializer.serializeTrustedConfiguration(oldConfigurationString);
            if (oldConfiguration == null) {
                throw new ConfigurationException();
            }
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Configuration loading aborted: failed to serialize last loaded " +
                    "configuration. This may mean that configuration structure has changed since last configuration load", e);
        }

        if (configurationExplorer.getConfiguration().equals(oldConfiguration)) {
            return;
        }

        RecursiveMerger recursiveMerger = new RecursiveMerger(oldConfiguration);
        recursiveMerger.merge();
        saveConfiguration();
    }

    private void saveConfiguration() {
        String configurationString =
                ConfigurationSerializer.deserializeConfiguration(configurationExplorer.getConfiguration());
        configurationDao.save(configurationString);
    }

    private class RecursiveLoader {
        private final Set<String> loadedDomainObjectConfigs = new HashSet<>();

        private RecursiveLoader() {
        }

        public void load() {
            Collection<DomainObjectConfig> configList = configurationExplorer.getDomainObjectConfigs();
            if(configList.isEmpty())  {
                return;
            }

            dataStructureDao.createServiceTables();

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

            dataStructureDao.createTable(domainObjectConfig);
            dataStructureDao.createSequence(domainObjectConfig);

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

    private Boolean isConfigurationLoaded() {
        Integer tablesCount = dataStructureDao.countTables();
        if(tablesCount == null) {
            throw new FatalException("Error occurred when calling DataStructureDao for tables count");
        }

        return tablesCount > 0;
    }

    private class RecursiveMerger {

        private final ConfigurationExplorer oldConfigurationExplorer;
        private final Set<String> mergedDomainObjectConfigs = new HashSet<>();

        private RecursiveMerger(Configuration oldConfiguration) {
            oldConfigurationExplorer = new ConfigurationExplorerImpl(oldConfiguration);
            oldConfigurationExplorer.build();
        }

        public void merge() {
            Collection<DomainObjectConfig> configList = configurationExplorer.getDomainObjectConfigs();
            if(configList.isEmpty())  {
                return;
            }

            validateForDeletedConfigurations();

            for (DomainObjectConfig config : configList) {
                merge(config);
            }
        }

        private void merge(DomainObjectConfig domainObjectConfig) {
            if(mergedDomainObjectConfigs.contains(domainObjectConfig.getName())) { // skip if already merged
                return;
            }

            DomainObjectConfig oldDomainObjectConfig =
                    oldConfigurationExplorer.getDomainObjectConfig(domainObjectConfig.getName());

            if (oldDomainObjectConfig == null) {
                loadDomainObjectConfig(domainObjectConfig);
            } else if (!domainObjectConfig.equals(oldDomainObjectConfig)) {
                String parentConfigName = domainObjectConfig.getParentConfig();
                String oldParentConfigName = oldDomainObjectConfig.getParentConfig();
                if ((parentConfigName == null && oldParentConfigName != null) ||
                        (parentConfigName != null && !parentConfigName.equals(oldParentConfigName))) {
                    throw new ConfigurationException("Configuration loading aborted: parent was changed for " +
                            " '" + domainObjectConfig.getName() + ". " + COMMON_ERROR_MESSAGE);
                }
                updateDomainObjectConfig(domainObjectConfig, oldDomainObjectConfig);
            }

            mergedDomainObjectConfigs.add(domainObjectConfig.getName()); // add to merged configs set
        }

        private void loadDomainObjectConfig(DomainObjectConfig domainObjectConfig) {
            // First merge referenced domain object configurations
            mergeDependentDomainObjectConfigs(domainObjectConfig);

            dataStructureDao.createTable(domainObjectConfig);
            dataStructureDao.createSequence(domainObjectConfig);
        }

        private void mergeDependentDomainObjectConfigs(DomainObjectConfig domainObjectConfig) {
            for(FieldConfig fieldConfig : domainObjectConfig.getFieldConfigs()) {
                if((ReferenceFieldConfig.class.equals(fieldConfig.getClass()))) {
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    merge(configurationExplorer.getDomainObjectConfig(referenceFieldConfig.getType()));
                }
            }
        }

        private void updateDomainObjectConfig(DomainObjectConfig domainObjectConfig,
                                              DomainObjectConfig oldDomainObjectConfig) {
            mergeDependentDomainObjectConfigs(domainObjectConfig);

            List<FieldConfig> newFieldConfigs = new ArrayList<>();
            List<UniqueKeyConfig> newUniqueKeyConfigs = new ArrayList<>();

            for (FieldConfig fieldConfig : domainObjectConfig.getFieldConfigs()) {
                FieldConfig oldFieldConfig =
                        oldConfigurationExplorer.getFieldConfig(domainObjectConfig.getName(), fieldConfig.getName());

                if (oldFieldConfig == null) {
                    newFieldConfigs.add(fieldConfig);
                } else if (!fieldConfig.equals(oldFieldConfig)) {
                    throw new ConfigurationException("Configuration loading aborted: FieldConfig '" +
                            domainObjectConfig.getName() + "." + fieldConfig.getName() + " was changed. " +
                            COMMON_ERROR_MESSAGE);
                }
            }

            for (UniqueKeyConfig uniqueKeyConfig : domainObjectConfig.getUniqueKeyConfigs()) {
                if (!oldDomainObjectConfig.getUniqueKeyConfigs().contains(uniqueKeyConfig)) {
                    newUniqueKeyConfigs.add(uniqueKeyConfig);
                }
            }

            if (!newFieldConfigs.isEmpty() || ! newUniqueKeyConfigs.isEmpty()) {
                dataStructureDao.updateTableStructure(domainObjectConfig.getName(), newFieldConfigs, newUniqueKeyConfigs);
            }
        }

        private void validateForDeletedConfigurations() {
            for (DomainObjectConfig oldDomainObjectConfig : oldConfigurationExplorer.getDomainObjectConfigs()) {
                DomainObjectConfig domainObjectConfig =
                        configurationExplorer.getDomainObjectConfig(oldDomainObjectConfig.getName());
                if (domainObjectConfig == null) {
                    throw new ConfigurationException("Configuration loading aborted: DomainObject configuration '" +
                            oldDomainObjectConfig.getName() + "' was deleted. " + COMMON_ERROR_MESSAGE);
                }

                for (FieldConfig oldFieldConfig : oldDomainObjectConfig.getFieldConfigs()) {
                    FieldConfig fieldConfig = configurationExplorer.getFieldConfig(oldDomainObjectConfig.getName(),
                            oldFieldConfig.getName());
                    if (fieldConfig == null) {
                        throw new ConfigurationException("Configuration loading aborted: Field " +
                                "Configuration DomainObject '" + oldDomainObjectConfig.getName() + "." +
                                oldFieldConfig.getName() + "' was deleted. " + COMMON_ERROR_MESSAGE);
                    }
                }

                if(!domainObjectConfig.getUniqueKeyConfigs().containsAll(oldDomainObjectConfig.getUniqueKeyConfigs())) {
                    throw new ConfigurationException("Configuration loading aborted: some unique key " +
                            "Configuration of DomainObject '" + oldDomainObjectConfig.getName() + "' was deleted. " +
                            COMMON_ERROR_MESSAGE);
                }
            }
        }

    }

}
