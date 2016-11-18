package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.MigrationComponent;
import ru.intertrust.cm.core.business.api.Migrator;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.migration.MigrationScriptConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Миграционный класс для исправления уникальных ключей, содержащих ссылочное поле
 */
@MigrationComponent(name = "ToPerModuleMigrationMigrator")
public class ToPerModuleMigrationMigrator implements Migrator {

    private ConfigurationExplorer configurationExplorer;
    private DomainObjectDao doDao;
    private AccessControlService accessControlService;
    private CrudService crudService;

    public ToPerModuleMigrationMigrator() {
        configurationExplorer = SpringApplicationContext.getContext().getBean(ConfigurationExplorer.class);
        doDao = SpringApplicationContext.getContext().getBean(DomainObjectDao.class);
        accessControlService = SpringApplicationContext.getContext().getBean(AccessControlService.class);
    }

    @Override
    public void execute() {
        final AccessToken token = accessControlService.createSystemAccessToken("Migration Migrator");
        final List<DomainObject> logEntries = doDao.findAll("migration_log", token);
        long maxDbSequenceBeforeMigration = -1;
        for (DomainObject logEntry : logEntries) {
            Long seq = logEntry.getLong("sequence_number");
            if (seq > maxDbSequenceBeforeMigration) {
                maxDbSequenceBeforeMigration = seq;
            }
        }
        if (maxDbSequenceBeforeMigration == -1) {
            return;
        }

        //doDao.delete(doDao.find)
        HashMap<String, Long> maxAlreadyMigratedConfigSequenceByModule = new HashMap<>();
        // save only those that are less or equal to max sequence
        Collection<MigrationScriptConfig> migrationScriptConfigs = configurationExplorer.getConfigs(MigrationScriptConfig.class);
        for (MigrationScriptConfig config : migrationScriptConfigs) {
            String moduleName = config.getModuleName();
            final long sequenceNumber = config.getSequenceNumber();
            final Long maxSequence = maxAlreadyMigratedConfigSequenceByModule.get(moduleName);
            if (sequenceNumber <= maxDbSequenceBeforeMigration && (maxSequence == null || sequenceNumber > maxSequence)) {
                maxAlreadyMigratedConfigSequenceByModule.put(moduleName, (long) sequenceNumber);
            }
        }

        for (DomainObject logEntry : logEntries) {
            doDao.delete(logEntry.getId(), token);
        }
        for (Map.Entry<String, Long> entry : maxAlreadyMigratedConfigSequenceByModule.entrySet()) {
            final GenericDomainObject domainObject = new GenericDomainObject("migration_log");
            domainObject.setLong("sequence_number", entry.getValue());
            domainObject.setString("module_name", entry.getKey());
            doDao.save(domainObject, token);

        }
    }


}
