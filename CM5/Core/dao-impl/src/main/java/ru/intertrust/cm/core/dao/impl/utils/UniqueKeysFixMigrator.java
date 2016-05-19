package ru.intertrust.cm.core.dao.impl.utils;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.intertrust.cm.core.business.api.MigrationComponent;
import ru.intertrust.cm.core.business.api.Migrator;
import ru.intertrust.cm.core.business.api.dto.UniqueKeyInfo;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.config.UniqueKeyFieldConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.SchemaCache;
import ru.intertrust.cm.core.util.SpringApplicationContext;

/**
 * Миграционный класс для исправления уникальных ключей, содержащих ссылочное поле
 */
@MigrationComponent(name = "UniqueKeysFix")
public class UniqueKeysFixMigrator implements Migrator {

    private DataStructureDao dataStructureDao;
    private ConfigurationExplorer configurationExplorer;
    private SchemaCache schemaCache;

    public UniqueKeysFixMigrator() {
        dataStructureDao = SpringApplicationContext.getContext().getBean(DataStructureDao.class);
        configurationExplorer = SpringApplicationContext.getContext().getBean(ConfigurationExplorer.class);
        schemaCache = SpringApplicationContext.getContext().getBean(SchemaCache.class);
    }

    /**
     * Выполняет действия миграционного компонента
     */
    @Override
    public void execute() {
        Collection<DomainObjectTypeConfig> domainObjectTypeConfigs =
                configurationExplorer.getConfigs(DomainObjectTypeConfig.class);

        for (DomainObjectTypeConfig domainObjectTypeConfig : domainObjectTypeConfigs) {
            List<UniqueKeyConfig> uniqueKeyConfigs = domainObjectTypeConfig.getUniqueKeyConfigs();

            for (UniqueKeyConfig uniqueKeyConfig : uniqueKeyConfigs) {
                String uniqueKeyName = getUniqueKeyName(domainObjectTypeConfig, uniqueKeyConfig);
                if (uniqueKeyName != null) {
                    dataStructureDao.dropConstraint(domainObjectTypeConfig, uniqueKeyName);
                }
            }

            dataStructureDao.createUniqueConstraints(domainObjectTypeConfig, uniqueKeyConfigs);
        }
    }

    private String getUniqueKeyName(DomainObjectTypeConfig config, UniqueKeyConfig keyConfig) {
        Collection<UniqueKeyInfo> domainObjectTypeKeys = schemaCache.getUniqueKeys(config);
        if (domainObjectTypeKeys == null || keyConfig.getUniqueKeyFieldConfigs() == null) {
            return null;
        }

        List<String> uniqueKeyFields = getUniqueKeyFields(config, keyConfig);

        for (UniqueKeyInfo uniqueKeyInfo : domainObjectTypeKeys) {
            if (uniqueKeyInfo.getColumnNames() == null || uniqueKeyInfo.getColumnNames().size() != uniqueKeyFields.size()) {
                continue;
            }

            boolean fieldsMatch = true;
            for (String fieldName : uniqueKeyFields) {
                if (!uniqueKeyInfo.getColumnNames().contains(fieldName)) {
                    fieldsMatch = false;
                    break;
                }
            }

            if (fieldsMatch) {
                return uniqueKeyInfo.getName();
            }
        }

        return null;
    }

    private List<String> getUniqueKeyFields(DomainObjectTypeConfig config, UniqueKeyConfig uniqueKeyConfig) {
        List<String> uniqueKeyFields = new ArrayList<>(uniqueKeyConfig.getUniqueKeyFieldConfigs().size());
        for (UniqueKeyFieldConfig uniqueKeyFieldConfig : uniqueKeyConfig.getUniqueKeyFieldConfigs()) {
            uniqueKeyFields.add(getSqlName(uniqueKeyFieldConfig.getName()));
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(config.getName(), uniqueKeyFieldConfig.getName());
            if (fieldConfig instanceof ReferenceFieldConfig){
                uniqueKeyFields.add(getSqlName(uniqueKeyFieldConfig.getName() + "_type"));
            }else if(fieldConfig instanceof DateTimeWithTimeZoneFieldConfig){
                uniqueKeyFields.add(getSqlName(uniqueKeyFieldConfig.getName() + "_tz"));
            }
        }

        return uniqueKeyFields;
    }
}
