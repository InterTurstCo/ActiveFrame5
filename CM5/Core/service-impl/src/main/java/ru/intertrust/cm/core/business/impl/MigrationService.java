package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.migration.*;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;
import ru.intertrust.cm.core.dao.api.*;

import java.util.*;

/**
 * Выполняет скриптовую миграцию
 */
public class MigrationService {
    private final static Logger logger = LoggerFactory.getLogger(MigrationService.class);
    private static final String MIGRATION_LOG_DO_TYPE_NAME = "migration_log";
    private static final String SEQUENCE_NUMBER_FIELD_NAME = "sequence_number";
    private static final String MODULE_NAME_FIELD_NAME = "module_name";

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DataStructureDao dataStructureDao;

    @Autowired
    private DomainObjectTypeIdDao domainObjectTypeIdDao;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private SqlLoggerEnforcer sqlLoggerEnforcer;

    @Autowired
    private SchemaCache schemaCache;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ServerComponentService serverComponentService;

    /**
     * Выполняет скриптовую миграцию до автоматической конфигурации
     */
    public boolean executeBeforeAutoMigration(ConfigurationExplorer oldConfigurationExplorer) {
        // todo: drop this code after everyone gets migrated to new version of Platform - it's a workaround for the case when there's no new column in DB
        if (oldConfigurationExplorer.getFieldConfig("migration_log", "module_name") == null) { // force migration
            performMigrationMechanismUpgrade(oldConfigurationExplorer);
        }

        return executeScriptMigration(oldConfigurationExplorer, true);
    }

    private void performMigrationMechanismUpgrade(ConfigurationExplorer oldConfigurationExplorer) {
        final List<MigrationScriptConfig> scriptConfigs = (List<MigrationScriptConfig>) configurationExplorer.getConfigs(MigrationScriptConfig.class);
        MigrationScriptConfig migrationMigrationScript = null;
        for (MigrationScriptConfig scriptConfig : scriptConfigs) {
            if (scriptConfig.getSequenceNumber() == 100 && "core".equals(scriptConfig.getModuleName())) {
                migrationMigrationScript = scriptConfig;
                break;
            }
        }
        if (migrationMigrationScript == null) {
            throw new IllegalArgumentException("No script for Migration of Migration found");
        }
        executeAutoMigrationEvent(migrationMigrationScript.getSequenceNumber(), migrationMigrationScript.getBeforeAutoMigrationConfig(), oldConfigurationExplorer);
    }

    /**
     * Выполняет скриптовую миграцию после автоматической конфигурации
     */
    public boolean executeAfterAutoMigration(ConfigurationExplorer oldConfigurationExplorer) {
        return executeScriptMigration(oldConfigurationExplorer, false);
    }

    public void writeMigrationLog() {
        Collection<MigrationScriptConfig> migrationConfigs = configurationExplorer.getConfigs(MigrationScriptConfig.class);
        HashMap<String, Long> maxConfigSequenceByModule = new HashMap<>();
        for (MigrationScriptConfig migrationConfig : migrationConfigs) {
            final String moduleName = migrationConfig.getModuleName();
            final int sequenceNumber = migrationConfig.getSequenceNumber();
            final Long maxSequence = maxConfigSequenceByModule.get(moduleName);
            if (maxSequence == null || sequenceNumber > maxSequence) {
                maxConfigSequenceByModule.put(moduleName, (long) sequenceNumber);
            }
        }
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            Long migrationVersion = maxConfigSequenceByModule.get(moduleConfiguration.getName());
            final long maxSavedSequenceNumber = getMaxSavedMigrationSequenceNumber(moduleConfiguration);
            if (migrationVersion == null || migrationVersion <= maxSavedSequenceNumber) {
                continue;
            }
            DomainObject migrationLog = crudService.createDomainObject(MIGRATION_LOG_DO_TYPE_NAME);
            migrationLog.setLong(SEQUENCE_NUMBER_FIELD_NAME, migrationVersion);
            migrationLog.setString(MODULE_NAME_FIELD_NAME, moduleConfiguration.getName());
            crudService.save(migrationLog);
        }

    }

    /**
     * Возвращает максимальный номер миграционного скрипта из базы данных
     * @return
     * @param moduleConfiguration
     */
    public long getMaxSavedMigrationSequenceNumber(ModuleConfiguration moduleConfiguration) {
        if (!dataStructureDao.isTableExist("migration_log")) {
            return 0;
        }

        final Filter byModule = Filter.create("byModule", 0, new StringValue(moduleConfiguration.getName()));
        IdentifiableObjectCollection collection = collectionsService.findCollection("last_migration_log", null, Collections.singletonList(byModule));
        if (collection == null || collection.size() == 0) {
            return 0;
        }

        return collection.get(collection.size() - 1).getLong(SEQUENCE_NUMBER_FIELD_NAME);
    }

    private boolean executeScriptMigration(ConfigurationExplorer oldConfigurationExplorer, boolean beforeAutoMigration) {
        Collection<MigrationScriptConfig> migrationConfigs = configurationExplorer.getConfigs(MigrationScriptConfig.class);
        sqlLoggerEnforcer.forceSqlLogging();
        boolean migrationDone = false;
        for (ModuleConfiguration moduleConfiguration : moduleService.getModuleList()) {
            final ArrayList<MigrationScriptConfig> migrationScriptConfigList = getModuleMigrations(moduleConfiguration);
            if (migrationScriptConfigList == null || migrationScriptConfigList.size() == 0) {
                logger.warn("Module: " + moduleConfiguration.getName() + ". No " + (beforeAutoMigration ? "Before" : "After") + " Auto-Migration Scripts to execute");
                continue;
            }
            logger.warn("Module: " + moduleConfiguration.getName() + ". " + "Starting " + (beforeAutoMigration ? "Before" : "After") + " Auto-Migration Scripts......................");

            Collections.sort(migrationScriptConfigList, new MigrationScriptSequenceComparator());

            // get all migrations for the module (in case of 1st Core migration - returns 0)
            long lastSavedMigrationSequence = getMaxSavedMigrationSequenceNumber(moduleConfiguration);
            for (MigrationScriptConfig migrationScriptConfig : migrationScriptConfigList) {
                if (migrationScriptConfig.getSequenceNumber() <= lastSavedMigrationSequence) {
                    continue;
                }

                if (beforeAutoMigration) {
                    executeAutoMigrationEvent(migrationScriptConfig.getSequenceNumber(), migrationScriptConfig.getBeforeAutoMigrationConfig(), oldConfigurationExplorer);
                } else {
                    executeAutoMigrationEvent(migrationScriptConfig.getSequenceNumber(), migrationScriptConfig.getAfterAutoMigrationConfig(), oldConfigurationExplorer);
                }

                migrationDone = true;
            }
            logger.warn("Module: " + moduleConfiguration.getName() + ". " + "Done " + (beforeAutoMigration ? "Before" : "After") + " Auto-Migration Scripts......................");
        }

        sqlLoggerEnforcer.cancelSqlLoggingEnforcement();
        logger.warn("Done " + (beforeAutoMigration ? "Before" : "After") + " Auto-Migration Scripts......................");
        return migrationDone;
    }

    private ArrayList<MigrationScriptConfig> getModuleMigrations(ModuleConfiguration moduleConfiguration) {
        Collection<MigrationScriptConfig> allMigrationConfigs = configurationExplorer.getConfigs(MigrationScriptConfig.class);
        ArrayList<MigrationScriptConfig> moduleMigrationConfigs = new ArrayList<>();
        for (MigrationScriptConfig config : allMigrationConfigs) {
            if (moduleConfiguration.getName().equals(config.getModuleName())) {
                moduleMigrationConfigs.add(config);
            }
        }
        Collections.sort(moduleMigrationConfigs, new MigrationScriptSequenceComparator());
        return moduleMigrationConfigs;
    }

    private void executeAutoMigrationEvent(int sequenceNumber, AutoMigrationEventConfig autoMigrationEventConfig,
                                           ConfigurationExplorer oldConfigurationExplorer) {
        if (autoMigrationEventConfig == null) {
            return;
        }
        if (logger.isWarnEnabled()) {
            logger.warn("Executing script: " + sequenceNumber);
        }
        for (MigrationScenarioConfig scenario : autoMigrationEventConfig.getMigrationScenarioConfigs()) {
            if (scenario instanceof MakeNotNullConfig) {
                processMakeNotNull((MakeNotNullConfig) scenario);
            } else if (scenario instanceof ChangeFieldClassConfig) {
                processChangeFieldsTypes((ChangeFieldClassConfig) scenario, oldConfigurationExplorer);
            } else if (scenario instanceof ExecuteConfig) {
                processMigrationComponent((ExecuteConfig) scenario);
            } else if (scenario instanceof NativeCommandConfig) {
                processNativeCommand((NativeCommandConfig) scenario);
            } else if (scenario instanceof CreateUniqueKeyConfig) {
                processCreateUniqueKey((CreateUniqueKeyConfig) scenario);
            } else if (scenario instanceof RenameFieldConfig) {
                processRenameFields((RenameFieldConfig) scenario);
            } else if (scenario instanceof DeleteFieldsConfig) {
                processDeleteFields((DeleteFieldsConfig) scenario, oldConfigurationExplorer);
            } else if (scenario instanceof DeleteTypesConfig) {
                processDeleteDOTypes((DeleteTypesConfig) scenario, oldConfigurationExplorer);
            } else if (scenario instanceof UnextendTypesConfig) {
                processUnextendDOTypes((UnextendTypesConfig) scenario, oldConfigurationExplorer);
            }

        }
    }

    private void processChangeFieldsTypes(ChangeFieldClassConfig changeFieldClassConfig,
                                          ConfigurationExplorer oldConfigurationExplorer) {
        if (changeFieldClassConfig.getFields() == null) {
            return;
        }

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(changeFieldClassConfig.getType());

        if (domainObjectTypeConfig == null) {
            throw new ConfigurationException("Failed to change field of DO type " +
                    domainObjectTypeConfig.getName() + " because it doesn't exist");
        }

        for (ChangeFieldClassFieldConfig changeFieldClassFieldConfig : changeFieldClassConfig.getFields()) {
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(changeFieldClassConfig.getType(),
                    changeFieldClassFieldConfig.getName());
            FieldConfig oldFieldConfig = oldConfigurationExplorer.getFieldConfig(changeFieldClassConfig.getType(),
                    changeFieldClassFieldConfig.getName());

            if (fieldConfig == null) {
                throw new ConfigurationException("Failed to change field " + domainObjectTypeConfig.getName() +
                        "." + changeFieldClassFieldConfig.getName() + " type because it is not found in " +
                        "configuration");
            }

            if (oldFieldConfig == null) {
                throw new ConfigurationException("Failed to change field " + domainObjectTypeConfig.getName() +
                        "." + changeFieldClassFieldConfig.getName() + " type because it doesn't exist");
            }

            if (!fieldConfig.getClass().equals(oldFieldConfig.getClass()) &&
                    !((fieldConfig instanceof StringFieldConfig && oldFieldConfig instanceof  StringFieldConfig) ||
                            (fieldConfig instanceof StringFieldConfig && oldFieldConfig instanceof  TextFieldConfig) ||
                            (fieldConfig instanceof TextFieldConfig && oldFieldConfig instanceof  StringFieldConfig) ||
                            (fieldConfig instanceof StringFieldConfig && oldFieldConfig instanceof  BooleanFieldConfig) ||
                            (fieldConfig instanceof BooleanFieldConfig && oldFieldConfig instanceof  StringFieldConfig) ||
                            (fieldConfig instanceof LongFieldConfig && oldFieldConfig instanceof  DecimalFieldConfig) ||
                            (fieldConfig instanceof DecimalFieldConfig && oldFieldConfig instanceof  LongFieldConfig))) {
                throw new ConfigurationException("Failed to change field " + domainObjectTypeConfig.getName() +
                        "." + changeFieldClassFieldConfig.getName() + " type because conversion from " +
                        oldFieldConfig.getClass().getName() + " to " + fieldConfig.getClass().getName() +
                        " is not supported");
            }

            dataStructureDao.updateColumnType(domainObjectTypeConfig, oldFieldConfig, fieldConfig);
        }
    }

    private void processMakeNotNull(MakeNotNullConfig makeNotNullConfig) {
        if (makeNotNullConfig.getFields() == null) {
            return;
        }

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(makeNotNullConfig.getType());

        if (domainObjectTypeConfig == null) {
            throw new ConfigurationException("Failed to make not null field of DO type " +
                    makeNotNullConfig.getType() + " because it doesn't exist");
        }

        for (MakeNotNullFieldConfig makeNotNullFieldConfig : makeNotNullConfig.getFields()) {
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(makeNotNullConfig.getType(),
                    makeNotNullFieldConfig.getName());

            if (fieldConfig == null) {
                throw new ConfigurationException("Failed to make not null field " +
                        makeNotNullConfig.getType() + "." + makeNotNullFieldConfig.getName() + " because it doesn't exist");
            }

            dataStructureDao.setColumnNotNull(domainObjectTypeConfig, fieldConfig, true);
        }
    }

    private void processCreateUniqueKey(CreateUniqueKeyConfig createUniqueKeyConfig) {
        if (createUniqueKeyConfig.getFields() == null) {
            return;
        }

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(createUniqueKeyConfig.getType());

        if (domainObjectTypeConfig == null) {
            throw new ConfigurationException("Failed to create unique keys for DO type " +
                    createUniqueKeyConfig.getType() + " because it doesn't exist");
        }

        UniqueKeyConfig uniqueKeyConfig = new UniqueKeyConfig();
        for (CreateUniqueKeyFieldConfig createUniqueKeyFieldConfig : createUniqueKeyConfig.getFields()) {
            if (configurationExplorer.getFieldConfig(createUniqueKeyConfig.getType(),
                    createUniqueKeyFieldConfig.getName()) == null) {
                throw new ConfigurationException("Failed to create unique keys for DO type " +
                        createUniqueKeyConfig.getType() + "." + createUniqueKeyFieldConfig.getName() +
                        " because it doesn't exist");
            }

            UniqueKeyFieldConfig uniqueKeyFieldConfig = new UniqueKeyFieldConfig();
            uniqueKeyFieldConfig.setName(createUniqueKeyFieldConfig.getName());
            uniqueKeyConfig.getUniqueKeyFieldConfigs().add(uniqueKeyFieldConfig);
        }

        dataStructureDao.createForeignKeyAndUniqueConstraints(domainObjectTypeConfig,
                new ArrayList<ReferenceFieldConfig>(), Collections.singletonList(uniqueKeyConfig));
    }

    private void processDeleteFields(DeleteFieldsConfig deleteFieldsConfig,
                                     ConfigurationExplorer oldConfigurationExplorer) {
        if (deleteFieldsConfig.getFields() == null) {
            return;
        }

        DomainObjectTypeConfig domainObjectTypeConfig =
                oldConfigurationExplorer.getDomainObjectTypeConfig(deleteFieldsConfig.getType());

        if (domainObjectTypeConfig == null) {
            throw new ConfigurationException("Failed to delete fields of DO type " + deleteFieldsConfig.getType() +
                    " because it doesn't exist");
        }

        for (DeleteFieldsFieldConfig deleteFieldsFieldConfig : deleteFieldsConfig.getFields()) {
            FieldConfig fieldConfig =
                    oldConfigurationExplorer.getFieldConfig(deleteFieldsConfig.getType(), deleteFieldsFieldConfig.getName());

            if (fieldConfig == null) {
                throw new ConfigurationException("Failed to delete DO type field " + deleteFieldsConfig.getType() +
                        "." + deleteFieldsFieldConfig.getName() + " because it doesn't exist");
            }

            dataStructureDao.deleteColumn(domainObjectTypeConfig, fieldConfig);
        }
    }

    private void processRenameFields(RenameFieldConfig renameFieldConfig) {
        if (renameFieldConfig.getFields() == null) {
            return;
        }

        DomainObjectTypeConfig domainObjectTypeConfig =
                configurationExplorer.getDomainObjectTypeConfig(renameFieldConfig.getType());

        if (domainObjectTypeConfig == null) {
            throw new ConfigurationException("Failed to rename fields of DO type " + renameFieldConfig.getType() +
                    " because it doesn't exist");
        }

        for (RenameFieldFieldConfig renameFieldFieldConfig : renameFieldConfig.getFields()) {
            FieldConfig fieldConfig =
                    configurationExplorer.getFieldConfig(renameFieldConfig.getType(), renameFieldFieldConfig.getNewName());

            if (fieldConfig == null) {
                throw new ConfigurationException("Failed to rename field " + renameFieldConfig.getType() +
                        "." + renameFieldFieldConfig.getNewName() + " because it doesn't exist");
            }

            dataStructureDao.renameColumn(domainObjectTypeConfig, renameFieldFieldConfig.getName(), fieldConfig);
        }
    }

    private void processDeleteDOTypes(DeleteTypesConfig deleteTypesConfig,
                                      ConfigurationExplorer oldConfigurationExplorer) {
        if (deleteTypesConfig.getTypes() == null) {
            return;
        }

        for (DeleteTypesTypeConfig deleteTypesTypeConfig : deleteTypesConfig.getTypes()) {
            DomainObjectTypeConfig domainObjectTypeConfig =
                    oldConfigurationExplorer.getDomainObjectTypeConfig(deleteTypesTypeConfig.getName());

            if (domainObjectTypeConfig == null) {
                throw new ConfigurationException("Failed to delete DO type " + deleteTypesTypeConfig.getName() +
                        " because it doesn't exist");
            }

            dataStructureDao.deleteTypeTables(domainObjectTypeConfig);
            domainObjectTypeIdDao.delete(oldConfigurationExplorer.getDomainObjectTypeConfig(domainObjectTypeConfig.getName() + "_al"));
            domainObjectTypeIdDao.delete(domainObjectTypeConfig);
        }
    }

    private void processUnextendDOTypes(UnextendTypesConfig unextendTypesConfig, ConfigurationExplorer oldConfigurationExplorer) {
        schemaCache.reset();
        if (unextendTypesConfig.getTypes() == null) {
            return;
        }

        for (UnextendTypesTypeConfig unextendTypesTypeConfig : unextendTypesConfig.getTypes()) {
            final String typeName = unextendTypesTypeConfig.getName();
            DomainObjectTypeConfig domainObjectTypeConfig = oldConfigurationExplorer.getDomainObjectTypeConfig(typeName);
            if (domainObjectTypeConfig == null) {
                throw new ConfigurationException("Failed to unextend DO type " + typeName + " because it doesn't exist");
            }
            if (oldConfigurationExplorer.isAuditLogType(typeName)) {
                throw new ConfigurationException("Failed to unextend DO type: " + typeName + ". Audit log types are unextended automatically. Use base-type in migration script config");
            }
            unextendType(domainObjectTypeConfig);
            unextendType(oldConfigurationExplorer.getDomainObjectTypeConfig(domainObjectTypeConfig.getName() + Configuration.AUDIT_LOG_SUFFIX));
        }
    }

    private void unextendType(DomainObjectTypeConfig oldDomainObjectTypeConfig) {
        final String parentTypeForeignKeyName = schemaCache.getParentTypeForeignKeyName(oldDomainObjectTypeConfig);
        if (parentTypeForeignKeyName == null) {
            throw new ConfigurationException("Failed to unextend DO type " + oldDomainObjectTypeConfig.getName() + " as there's no foreign key to parent type");
        }
        this.dataStructureDao.dropConstraint(oldDomainObjectTypeConfig, parentTypeForeignKeyName);
        this.dataStructureDao.createSequence(configurationExplorer.getDomainObjectTypeConfig(oldDomainObjectTypeConfig.getName()));
    }

    private void processMigrationComponent(ExecuteConfig executeConfig) {
        ((Migrator) serverComponentService.getServerComponent(executeConfig.getComponentName())).execute();
    }

    private void processNativeCommand(NativeCommandConfig nativeCommandConfig) {
        if (nativeCommandConfig.getValue() != null && !nativeCommandConfig.getValue().trim().isEmpty()) {
            dataStructureDao.executeSqlQuery(nativeCommandConfig.getValue());
        }
    }

    private class MigrationScriptSequenceComparator implements Comparator<MigrationScriptConfig> {
        @Override
        public int compare(MigrationScriptConfig o1, MigrationScriptConfig o2) {
            return Integer.valueOf(o1.getSequenceNumber()).compareTo(o2.getSequenceNumber());
        }
    }
}
