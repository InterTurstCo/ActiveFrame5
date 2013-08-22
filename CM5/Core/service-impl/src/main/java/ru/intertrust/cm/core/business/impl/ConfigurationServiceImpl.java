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

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.util.*;

/**
 * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationService}
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
@Stateless
@Local(ConfigurationService.class)
@Remote(ConfigurationService.Remote.class)
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
    @Autowired
    private ConfigurationSerializer configurationSerializer;

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

    public void setConfigurationSerializer(ConfigurationSerializer configurationSerializer) {
        this.configurationSerializer = configurationSerializer;
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationService#loadConfiguration()}
     */
    @Override
    public void loadConfiguration() throws ConfigurationException {
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
            oldConfiguration = configurationSerializer.deserializeTrustedConfiguration(oldConfigurationString);
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
                ConfigurationSerializer.serializeConfiguration(configurationExplorer.getConfiguration());
        configurationDao.save(configurationString);
    }

    private class RecursiveLoader extends AbstractRecursiveLoader {

        private RecursiveLoader() {
        }

        public void load() {
            Collection<DomainObjectTypeConfig> configList =
                    configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
            if(configList.isEmpty())  {
                return;
            }

            dataStructureDao.createServiceTables();
            processConfigs(configList);
            createAclTables(configList);
        }

        @Override
        protected void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            loadDomainObjectConfig(domainObjectTypeConfig);
        }

        private void createAclTables(Collection<DomainObjectTypeConfig> configList) {
            for (DomainObjectTypeConfig config : configList) {
                if (!config.isTemplate()) {
                    createAclTablesFor(config);
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

    private class RecursiveMerger extends AbstractRecursiveLoader {

        private final ConfigurationExplorer oldConfigExplorer;

        private RecursiveMerger(Configuration oldConfiguration) {
            oldConfigExplorer = new ConfigurationExplorerImpl(oldConfiguration);
            oldConfigExplorer.build();
        }

        @Override
        protected void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            merge(domainObjectTypeConfig);
        }

        @Override
        protected void loadDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            super.loadDomainObjectConfig(domainObjectTypeConfig);
            createAclTablesFor(domainObjectTypeConfig);
        }

        private void merge() {
            Collection<DomainObjectTypeConfig> configList =
                    configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
            if(configList.isEmpty())  {
                return;
            }

            validateForDeletedConfigurations();
            processConfigs(configList);
        }

        private void merge(DomainObjectTypeConfig domainObjectTypeConfig) {
            DomainObjectTypeConfig oldDomainObjectTypeConfig =
                    oldConfigExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectTypeConfig.getName());

            if (oldDomainObjectTypeConfig == null) {
                loadDomainObjectConfig(domainObjectTypeConfig);
            } else if (!domainObjectTypeConfig.equals(oldDomainObjectTypeConfig)) {
                validateExtendsAttribute(domainObjectTypeConfig, oldDomainObjectTypeConfig);
                validateParentConfig(domainObjectTypeConfig, oldDomainObjectTypeConfig);
                updateDomainObjectConfig(domainObjectTypeConfig, oldDomainObjectTypeConfig);
            }
        }

        private void updateDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig,
                                              DomainObjectTypeConfig oldDomainObjectTypeConfig) {
            processDependentConfigs(domainObjectTypeConfig);

            List<FieldConfig> newFieldConfigs = new ArrayList<>();
            List<UniqueKeyConfig> newUniqueKeyConfigs = new ArrayList<>();

            for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                FieldConfig oldFieldConfig = oldConfigExplorer.getFieldConfig(domainObjectTypeConfig.getName(),
                        fieldConfig.getName(), false);

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
            for (DomainObjectTypeConfig oldDOTypeConfig : oldConfigExplorer.getConfigs(DomainObjectTypeConfig.class)) {
                DomainObjectTypeConfig domainObjectTypeConfig =
                        configurationExplorer.getConfig(DomainObjectTypeConfig.class, oldDOTypeConfig.getName());
                if (domainObjectTypeConfig == null) {
                    throw new ConfigurationException("Configuration loading aborted: DomainObject configuration '" +
                            oldDOTypeConfig.getName() + "' was deleted. " + COMMON_ERROR_MESSAGE);
                }

                for (FieldConfig oldFieldConfig : oldDOTypeConfig.getFieldConfigs()) {
                    FieldConfig fieldConfig = configurationExplorer.getFieldConfig(oldDOTypeConfig.getName(),
                            oldFieldConfig.getName(), false);
                    if (fieldConfig == null) {
                        throw new ConfigurationException("Configuration loading aborted: Field " +
                                "Configuration DomainObject '" + oldDOTypeConfig.getName() + "." +
                                oldFieldConfig.getName() + "' was deleted. " + COMMON_ERROR_MESSAGE);
                    }
                }

                if (!domainObjectTypeConfig.getUniqueKeyConfigs().containsAll(oldDOTypeConfig
                        .getUniqueKeyConfigs())) {
                    throw new ConfigurationException("Configuration loading aborted: some unique key " +
                            "Configuration of DomainObject '" + oldDOTypeConfig.getName() + "' was deleted. " +
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

    private abstract class AbstractRecursiveLoader {

        private final Set<String> processedConfigs = new HashSet<>();
        private final Set<String> inProcessConfigs = new HashSet<>();

        protected final void processDependentConfigs(DomainObjectTypeConfig domainObjectTypeConfig) {
            if (domainObjectTypeConfig.getExtendsAttribute() != null) {
                DomainObjectTypeConfig parentConfig =
                        configurationExplorer.getConfig(DomainObjectTypeConfig.class,
                                domainObjectTypeConfig.getExtendsAttribute());
                processConfig(parentConfig);
            }

            DomainObjectParentConfig masterConfig = domainObjectTypeConfig.getParentConfig();
            if (masterConfig != null) {
                processConfig(configurationExplorer.getConfig(DomainObjectTypeConfig.class, masterConfig.getName()));
            }

            for(FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                if( !ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                    continue;
                }

                ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                for (ReferenceFieldTypeConfig typeConfig : referenceFieldConfig.getTypes()) {
                    processConfig(configurationExplorer.getConfig(DomainObjectTypeConfig.class, typeConfig.getName()));
                }

            }
        }

        protected void loadDomainObjectConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            processDependentConfigs(domainObjectTypeConfig);
            createDbStructures(domainObjectTypeConfig);
        }

        protected final void processConfigs(Collection<DomainObjectTypeConfig> configList) {
            for(DomainObjectTypeConfig config : configList) {
                processConfig(config);
            }
        }

        protected void createAclTablesFor(DomainObjectTypeConfig domainObjectTypeConfig) {
            dataStructureDao.createAclTables(domainObjectTypeConfig);
        }

        protected abstract void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig);

        private void processConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
            // пропускаем, если конфиг уже загружен или если он как раз загружается в этот момент
            if (isProcessed(domainObjectTypeConfig) || isInProcess(domainObjectTypeConfig)) {
                return;
            }

            setAsInProcess(domainObjectTypeConfig);
            doProcessConfig(domainObjectTypeConfig);

            setAsProcessed(domainObjectTypeConfig);
        }

        private void createDbStructures(DomainObjectTypeConfig domainObjectTypeConfig) {
            if (!domainObjectTypeConfig.isTemplate()) {
                dataStructureDao.createTable(domainObjectTypeConfig);
                dataStructureDao.createSequence(domainObjectTypeConfig);
            }
        }

        private boolean isProcessed(DomainObjectTypeConfig domainObjectTypeConfig) {
            return processedConfigs.contains(domainObjectTypeConfig.getName());
        }

        private void setAsProcessed(DomainObjectTypeConfig domainObjectTypeConfig) {
            inProcessConfigs.remove(domainObjectTypeConfig.getName());
            processedConfigs.add(domainObjectTypeConfig.getName());
        }

        private boolean isInProcess(DomainObjectTypeConfig domainObjectTypeConfig) {
            return inProcessConfigs.contains(domainObjectTypeConfig.getName());
        }

        private void setAsInProcess(DomainObjectTypeConfig domainObjectTypeConfig) {
            inProcessConfigs.add(domainObjectTypeConfig.getName());
        }
    }
}
