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
            Collection<DomainObjectTypeConfig> configList = configurationExplorer.getDomainObjectConfigs();
            if(configList.isEmpty())  {
                return;
            }

            dataStructureDao.createServiceTables();

            for(DomainObjectTypeConfig config : configList) {
                loadDomainObjectConfig(config);
            }
        }

        private void loadDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            if(loadedDomainObjectConfigs.contains(domainObjectTypeConfig.getName())) { // skip if already loaded
                return;
            }

            // First load referenced domain object configurations
            loadDependentDomainObjectConfigs(domainObjectTypeConfig);

            dataStructureDao.createTable(domainObjectTypeConfig);
            dataStructureDao.createSequence(domainObjectTypeConfig);

            loadedDomainObjectConfigs.add(domainObjectTypeConfig.getName()); // add to loaded configs set
        }

        private void loadDependentDomainObjectConfigs(DomainObjectTypeConfig domainObjectTypeConfig) {
            for(FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                if((ReferenceFieldConfig.class.equals(fieldConfig.getClass()))) {
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    loadDomainObjectConfig(configurationExplorer.getDomainObjectTypeConfig(referenceFieldConfig.getType()));
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
            Collection<DomainObjectTypeConfig> configList = configurationExplorer.getDomainObjectConfigs();
            if(configList.isEmpty())  {
                return;
            }

            validateForDeletedConfigurations();

            for (DomainObjectTypeConfig config : configList) {
                merge(config);
            }
        }

        private void merge(DomainObjectTypeConfig domainObjectTypeConfig) {
            if(mergedDomainObjectConfigs.contains(domainObjectTypeConfig.getName())) { // skip if already merged
                return;
            }

            DomainObjectTypeConfig oldDomainObjectTypeConfig =
                    oldConfigurationExplorer.getDomainObjectTypeConfig(domainObjectTypeConfig.getName());

            if (oldDomainObjectTypeConfig == null) {
                loadDomainObjectConfig(domainObjectTypeConfig);
            } else if (!domainObjectTypeConfig.equals(oldDomainObjectTypeConfig)) {
                validateExtendsAttribute(domainObjectTypeConfig, oldDomainObjectTypeConfig);
                validateParentConfig(domainObjectTypeConfig, oldDomainObjectTypeConfig);
                updateDomainObjectConfig(domainObjectTypeConfig, oldDomainObjectTypeConfig);
            }

            mergedDomainObjectConfigs.add(domainObjectTypeConfig.getName()); // add to merged configs set
        }

        private void loadDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            // First merge referenced domain object configurations
            mergeDependentDomainObjectConfigs(domainObjectTypeConfig);

            dataStructureDao.createTable(domainObjectTypeConfig);
            dataStructureDao.createSequence(domainObjectTypeConfig);
        }

        private void mergeDependentDomainObjectConfigs(DomainObjectTypeConfig domainObjectTypeConfig) {
            for(FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                if((ReferenceFieldConfig.class.equals(fieldConfig.getClass()))) {
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    merge(configurationExplorer.getDomainObjectTypeConfig(referenceFieldConfig.getType()));
                }
            }
        }

        private void updateDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig,
                                              DomainObjectTypeConfig oldDomainObjectTypeConfig) {
            mergeDependentDomainObjectConfigs(domainObjectTypeConfig);

            List<FieldConfig> newFieldConfigs = new ArrayList<>();
            List<UniqueKeyConfig> newUniqueKeyConfigs = new ArrayList<>();

            for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                FieldConfig oldFieldConfig =
                        oldConfigurationExplorer.getFieldConfig(domainObjectTypeConfig.getName(), fieldConfig.getName());

                if (oldFieldConfig == null) {
                    newFieldConfigs.add(fieldConfig);
                } else if (!fieldConfig.equals(oldFieldConfig)) {
                    throw new ConfigurationException("Configuration loading aborted: FieldConfig '" +
                            domainObjectTypeConfig.getName() + "." + fieldConfig.getName() + " was changed. " +
                            COMMON_ERROR_MESSAGE);
                }
            }

            for (UniqueKeyConfig uniqueKeyConfig : domainObjectTypeConfig.getUniqueKeyConfigs()) {
                if (!oldDomainObjectTypeConfig.getUniqueKeyConfigs().contains(uniqueKeyConfig)) {
                    newUniqueKeyConfigs.add(uniqueKeyConfig);
                }
            }

            DomainObjectParentConfig parentConfig = domainObjectTypeConfig.getParentConfig() != null &&
                    !domainObjectTypeConfig.getParentConfig().equals(oldDomainObjectTypeConfig.getParentConfig()) ?
                    domainObjectTypeConfig.getParentConfig() : null;

            if (!newFieldConfigs.isEmpty() || !newUniqueKeyConfigs.isEmpty()|| parentConfig != null) {
                dataStructureDao.updateTableStructure(domainObjectTypeConfig.getName(), newFieldConfigs,
                        newUniqueKeyConfigs, parentConfig);
            }
        }

        private void validateForDeletedConfigurations() {
            for (DomainObjectTypeConfig oldDomainObjectTypeConfig : oldConfigurationExplorer.getDomainObjectConfigs()) {
                DomainObjectTypeConfig domainObjectTypeConfig =
                        configurationExplorer.getDomainObjectTypeConfig(oldDomainObjectTypeConfig.getName());
                if (domainObjectTypeConfig == null) {
                    throw new ConfigurationException("Configuration loading aborted: DomainObject configuration '" +
                            oldDomainObjectTypeConfig.getName() + "' was deleted. " + COMMON_ERROR_MESSAGE);
                }

                for (FieldConfig oldFieldConfig : oldDomainObjectTypeConfig.getFieldConfigs()) {
                    FieldConfig fieldConfig = configurationExplorer.getFieldConfig(oldDomainObjectTypeConfig.getName(),
                            oldFieldConfig.getName());
                    if (fieldConfig == null) {
                        throw new ConfigurationException("Configuration loading aborted: Field " +
                                "Configuration DomainObject '" + oldDomainObjectTypeConfig.getName() + "." +
                                oldFieldConfig.getName() + "' was deleted. " + COMMON_ERROR_MESSAGE);
                    }
                }

                if (!domainObjectTypeConfig.getUniqueKeyConfigs().containsAll(oldDomainObjectTypeConfig
                        .getUniqueKeyConfigs())) {
                    throw new ConfigurationException("Configuration loading aborted: some unique key " +
                            "Configuration of DomainObject '" + oldDomainObjectTypeConfig.getName() + "' was deleted. " +
                            COMMON_ERROR_MESSAGE);
                }
            }
        }
    }

    private void validateExtendsAttribute(DomainObjectTypeConfig domainObjectTypeConfig,
                                          DomainObjectTypeConfig oldDomainObjectTypeConfig) {
        String extendsAttributeValue = domainObjectTypeConfig.getExtendsAttribute();
        String oldExtendsAttributeValue = oldDomainObjectTypeConfig.getExtendsAttribute();

        if ((extendsAttributeValue == null && oldExtendsAttributeValue != null) ||
                (extendsAttributeValue != null && !extendsAttributeValue.equals(oldExtendsAttributeValue))) {
            throw new ConfigurationException("Configuration loading aborted: 'extends' attribute was changed " +
                    "for '" + domainObjectTypeConfig.getName() + ". " + COMMON_ERROR_MESSAGE);
        }
    }

    private void validateParentConfig(DomainObjectTypeConfig domainObjectTypeConfig,
                                          DomainObjectTypeConfig oldDomainObjectTypeConfig) {
        DomainObjectParentConfig parentConfig = domainObjectTypeConfig.getParentConfig();
        DomainObjectParentConfig oldParentConfig = oldDomainObjectTypeConfig.getParentConfig();

        if ((parentConfig == null && oldParentConfig != null) ||
                (parentConfig != null && !parentConfig.equals(oldParentConfig))) {
            throw new ConfigurationException("Configuration loading aborted: parent config was changed " +
                    "for '" + domainObjectTypeConfig.getName() + ". " + COMMON_ERROR_MESSAGE);
        }
    }
}
