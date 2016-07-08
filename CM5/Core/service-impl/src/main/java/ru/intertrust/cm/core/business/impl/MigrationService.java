package ru.intertrust.cm.core.business.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.Migrator;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.migration.*;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.SchemaCache;
import ru.intertrust.cm.core.dao.api.SqlLoggerEnforcer;

import java.util.*;

/**
 * Выполняет скриптовую миграцию
 */
public class MigrationService {
    private final static Logger logger = LoggerFactory.getLogger(MigrationService.class);
    private static final String MIGRATION_LOG_DO_TYPE_NAME = "migration_log";
    private static final String SEQUENCE_NUMBER_FIELD_NAME = "sequence_number";

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

    /**
     * Выполняет скриптовую миграцию до автоматической конфигурации
     */
    public boolean executeBeforeAutoMigration(ConfigurationExplorer oldConfigurationExplorer) {
        return executeScriptMigration(oldConfigurationExplorer, true);
    }

    /**
     * Выполняет скриптовую миграцию после автоматической конфигурации
     */
    public boolean executeAfterAutoMigration(ConfigurationExplorer oldConfigurationExplorer) {
        return executeScriptMigration(oldConfigurationExplorer, false);
    }

    /**
     * Записывает данные о проведении скриптовой миграции
     */
    public void writeMigrationLog(long migrationVersion) {
        long maxSavedSequenceNumber = getMaxSavedMigrationSequenceNumber();
        if (migrationVersion <= maxSavedSequenceNumber) {
            return;
        }

        DomainObject migrationLog = crudService.createDomainObject(MIGRATION_LOG_DO_TYPE_NAME);
        migrationLog.setLong(SEQUENCE_NUMBER_FIELD_NAME, migrationVersion);
        crudService.save(migrationLog);
    }

    /**
     * Возвращает максимальный номер миграциооного скрипта из конфигурации
     * @return
     */
    public long getMaxMigrationSequenceNumberFromConfiguration() {
        long migrationVersion = 0;

        Collection<MigrationScriptConfig> migrationConfigs = configurationExplorer.getConfigs(MigrationScriptConfig.class);
        if (migrationConfigs != null && !migrationConfigs.isEmpty()) {
            MigrationScriptConfig maxSequenceConfig =
                    Collections.max(migrationConfigs, new MigrationScriptSequenceComparator());
            migrationVersion = maxSequenceConfig.getSequenceNumber();
        }

        return migrationVersion;
    }

    /**
     * Возвращает максимальный номер миграционного скрипта из базы данных
     * @return
     */
    public long getMaxSavedMigrationSequenceNumber() {
        if (!dataStructureDao.isTableExist("migration_log")) {
            return 0;
        }

        IdentifiableObjectCollection collection = collectionsService.findCollection("last_migration_log");
        if (collection == null || collection.size() == 0) {
            return 0;
        }

        return collection.get(collection.size() - 1).getLong(SEQUENCE_NUMBER_FIELD_NAME);
    }

    private boolean executeScriptMigration(ConfigurationExplorer oldConfigurationExplorer, boolean beforeAutoMigration) {
        Collection<MigrationScriptConfig> migrationConfigs = configurationExplorer.getConfigs(MigrationScriptConfig.class);
        if (migrationConfigs == null || migrationConfigs.size() == 0) {
            logger.warn("No " + (beforeAutoMigration ? "Before" : "After") + " Auto-Migration Scripts to execute");
            return false;
        }
        logger.warn("Starting " + (beforeAutoMigration ? "Before" : "After") + " Auto-Migration Scripts......................");

        List<MigrationScriptConfig> migrationScriptConfigList = new ArrayList<>(migrationConfigs);
        Collections.sort(migrationScriptConfigList, new MigrationScriptSequenceComparator());

        long lastSavedMigrationSequence = getMaxSavedMigrationSequenceNumber();

        boolean migrationDone = false;
        sqlLoggerEnforcer.forceSqlLogging();

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

        sqlLoggerEnforcer.cancelSqlLoggingEnforcement();
        logger.warn("Done " + (beforeAutoMigration ? "Before" : "After") + " Auto-Migration Scripts......................");
        return migrationDone;
    }

    private void executeAutoMigrationEvent(int sequenceNumber, AutoMigrationEventConfig autoMigrationEventConfig,
                                           ConfigurationExplorer oldConfigurationExplorer) {
        if (autoMigrationEventConfig == null) {
            return;
        }
        if (logger.isWarnEnabled()) {
            logger.warn("Executing script: " + sequenceNumber);
        }
        processMakeNotNull(autoMigrationEventConfig);
        processChangeFieldTypes(autoMigrationEventConfig, oldConfigurationExplorer);
        processMigrationComponents(autoMigrationEventConfig);
        processNativeCommands(autoMigrationEventConfig);
        processCreateUniqueKeys(autoMigrationEventConfig);
        processRenameFields(autoMigrationEventConfig);
        processDeleteFields(autoMigrationEventConfig, oldConfigurationExplorer);
        processDeleteDOTypes(autoMigrationEventConfig, oldConfigurationExplorer);
        processUnextendDOTypes(autoMigrationEventConfig, oldConfigurationExplorer);
    }

    private void processChangeFieldTypes(AutoMigrationEventConfig autoMigrationEventConfig,
                                         ConfigurationExplorer oldConfigurationExplorer) {
        if (autoMigrationEventConfig.getChangeFieldClassConfigs() == null) {
            return;
        }

        for (ChangeFieldClassConfig changeFieldClassConfig : autoMigrationEventConfig.getChangeFieldClassConfigs()) {
            if (changeFieldClassConfig.getFields() == null) {
                continue;
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

                dataStructureDao.updateColumnType(domainObjectTypeConfig, fieldConfig);
            }
        }
    }

    private void processMakeNotNull(AutoMigrationEventConfig autoMigrationEventConfig) {
        if (autoMigrationEventConfig.getMakeNotNullConfigs() == null) {
            return;
        }

        for (MakeNotNullConfig makeNotNullConfig : autoMigrationEventConfig.getMakeNotNullConfigs()) {
            if (makeNotNullConfig.getFields() == null) {
                continue;
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
    }

    private void processCreateUniqueKeys(AutoMigrationEventConfig autoMigrationEventConfig) {
        if (autoMigrationEventConfig.getCreateUniqueKeyConfigs() == null) {
            return;
        }

        for (CreateUniqueKeyConfig createUniqueKeyConfig : autoMigrationEventConfig.getCreateUniqueKeyConfigs()) {
            if (createUniqueKeyConfig.getFields() == null) {
                continue;
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
    }

    private void processDeleteFields(AutoMigrationEventConfig autoMigrationEventConfig,
                                     ConfigurationExplorer oldConfigurationExplorer) {
        if (autoMigrationEventConfig.getDeleteFieldsConfigs() == null) {
            return;
        }

        for (DeleteFieldsConfig deleteFieldsConfig : autoMigrationEventConfig.getDeleteFieldsConfigs()) {
            if (deleteFieldsConfig.getFields() == null) {
                continue;
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
    }

    private void processRenameFields(AutoMigrationEventConfig autoMigrationEventConfig) {
        if (autoMigrationEventConfig.getRenameFieldConfigs() == null) {
            return;
        }

        for (RenameFieldConfig renameFieldConfig : autoMigrationEventConfig.getRenameFieldConfigs()) {
            if (renameFieldConfig.getFields() == null) {
                continue;
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
    }

    private void processDeleteDOTypes(AutoMigrationEventConfig autoMigrationEventConfig,
                                      ConfigurationExplorer oldConfigurationExplorer) {
        if (autoMigrationEventConfig.getDeleteTypesConfigs() == null) {
            return;
        }

        for (DeleteTypesConfig deleteTypesConfig : autoMigrationEventConfig.getDeleteTypesConfigs()) {
            if (deleteTypesConfig.getTypes() == null) {
                continue;
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
    }

    private void processUnextendDOTypes(AutoMigrationEventConfig autoMigrationEventConfig, ConfigurationExplorer oldConfigurationExplorer) {
        final List<UnextendTypesConfig> unextendTypesConfigs = autoMigrationEventConfig.getUnextendTypesConfigs();
        if (unextendTypesConfigs == null || unextendTypesConfigs.isEmpty()) {
            return;
        }

        schemaCache.reset();
        for (UnextendTypesConfig unextendTypesConfig : unextendTypesConfigs) {
            if (unextendTypesConfig.getTypes() == null) {
                continue;
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
    }

    private void unextendType(DomainObjectTypeConfig oldDomainObjectTypeConfig) {
        final String parentTypeForeignKeyName = schemaCache.getParentTypeForeignKeyName(oldDomainObjectTypeConfig);
        if (parentTypeForeignKeyName == null) {
            throw new ConfigurationException("Failed to unextend DO type " + oldDomainObjectTypeConfig.getName() + " as there's no foreign key to parent type");
        }
        this.dataStructureDao.dropConstraint(oldDomainObjectTypeConfig, parentTypeForeignKeyName);
        this.dataStructureDao.createSequence(configurationExplorer.getDomainObjectTypeConfig(oldDomainObjectTypeConfig.getName()));
    }

    private void processMigrationComponents(AutoMigrationEventConfig autoMigrationEventConfig) {
        if (autoMigrationEventConfig.getExecuteConfigs() == null) {
            return;
        }

        ConfigurationClassesCache configurationClassesCache = ConfigurationClassesCache.getInstance();

        for (ExecuteConfig executeConfig : autoMigrationEventConfig.getExecuteConfigs()) {
            Class clazz = configurationClassesCache.getClassByMigrationComponentName(executeConfig.getComponentName());

            if (clazz == null) {
                throw new ConfigurationException("Failed to execute migration component " +
                        executeConfig.getComponentName() + " because it doesn't exist");
            }

            try {
                Object migrationComponentInstance = clazz.newInstance();
                if (!(migrationComponentInstance instanceof Migrator)) {
                    throw new ConfigurationException("Failed to execute migration component " +
                            executeConfig.getComponentName() + " because it doesn't implement Migrator interface");
                }

                ((Migrator) migrationComponentInstance).execute();
            } catch (InstantiationException|IllegalAccessException e) {
                throw new ConfigurationException("Failed to execute migration component " +
                        executeConfig.getComponentName(), e);
            }
        }
    }

    private void processNativeCommands(AutoMigrationEventConfig autoMigrationEventConfig) {
        if (autoMigrationEventConfig.getNativeCommandConfigs() == null) {
            return;
        }

        for (NativeCommandConfig nativeCommandConfig : autoMigrationEventConfig.getNativeCommandConfigs()) {
            if (nativeCommandConfig.getValue() != null && !nativeCommandConfig.getValue().trim().isEmpty()) {
                dataStructureDao.executeSqlQuery(nativeCommandConfig.getValue());
            }
        }
    }

    private class MigrationScriptSequenceComparator implements Comparator<MigrationScriptConfig> {
        @Override
        public int compare(MigrationScriptConfig o1, MigrationScriptConfig o2) {
            return Integer.valueOf(o1.getSequenceNumber()).compareTo(o2.getSequenceNumber());
        }
    }
}
