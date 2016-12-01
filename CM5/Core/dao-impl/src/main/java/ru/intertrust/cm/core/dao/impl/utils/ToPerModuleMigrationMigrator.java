package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.migration.MigrationScriptConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.api.Migrator;
import ru.intertrust.cm.core.dao.api.component.ServerComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Миграционный класс для исправления уникальных ключей, содержащих ссылочное поле
 */
@ServerComponent(name = "ToPerModuleMigrationMigrator")
public class ToPerModuleMigrationMigrator implements Migrator {

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private CrudService crudService;

    @Autowired
    private AccessControlService accessControlService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute() {
        HashMap<String, Long> maxAlreadyMigratedConfigSequenceByModule = new HashMap<>();
        // save only those that are less or equal to max sequence
        Collection<MigrationScriptConfig> migrationScriptConfigs = configurationExplorer.getConfigs(MigrationScriptConfig.class);
        for (MigrationScriptConfig config : migrationScriptConfigs) {
            String moduleName = config.getModuleName();
            final long sequenceNumber = config.getSequenceNumber();
            final Long maxSequence = maxAlreadyMigratedConfigSequenceByModule.get(moduleName);
            if (maxSequence == null || sequenceNumber > maxSequence) {
                maxAlreadyMigratedConfigSequenceByModule.put(moduleName, (long) sequenceNumber);
            }
        }

        final List<DomainObject> logEntries = crudService.findAll("migration_log");
        for (DomainObject logEntry : logEntries) {
            crudService.delete(logEntry.getId());
        }
        for (Map.Entry<String, Long> entry : maxAlreadyMigratedConfigSequenceByModule.entrySet()) {
            final GenericDomainObject domainObject = new GenericDomainObject("migration_log");
            domainObject.setLong("sequence_number", entry.getValue());
            domainObject.setString("module_name", entry.getKey());
            crudService.save(domainObject);

        }
    }


}
