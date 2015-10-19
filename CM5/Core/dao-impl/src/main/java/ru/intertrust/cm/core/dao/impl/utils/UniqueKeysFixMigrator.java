package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.MigrationComponent;
import ru.intertrust.cm.core.business.api.Migrator;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.UniqueKeyConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDao;
import ru.intertrust.cm.core.dao.api.SchemaCache;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.util.Collection;
import java.util.List;

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
                String uniqueKeyName = schemaCache.getUniqueKeyName(domainObjectTypeConfig, uniqueKeyConfig);
                if (uniqueKeyName != null) {
                    dataStructureDao.dropConstraint(domainObjectTypeConfig, uniqueKeyName);
                }
            }

            dataStructureDao.createUniqueConstraints(domainObjectTypeConfig, uniqueKeyConfigs);
        }
    }
}
