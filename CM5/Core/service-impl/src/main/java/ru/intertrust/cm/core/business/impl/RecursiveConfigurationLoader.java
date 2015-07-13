package ru.intertrust.cm.core.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import java.util.Collection;

/**
* Recursively loads configuration
* Designed as prototype, not thread-safe, instances are not reusable!!!
*/
public class RecursiveConfigurationLoader extends AbstractRecursiveConfigurationLoader {

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    /**
     * Recursively loads configuration from {@code ConfigurationExplorer}
     * @param configurationExplorer {@code ConfigurationExplorer} used to load configuration
     */
    public void load(ConfigurationExplorer configurationExplorer) {
        setConfigurationExplorer(configurationExplorer);

        Collection<DomainObjectTypeConfig> configList =
                configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        if(configList.isEmpty())  {
            return;
        }

        dataStructureDao.createServiceTables();
        domainObjectTypeIdCache.build();

        processConfigs(configList);
        createAclTables(configList);
    }

    @Override
    protected void doProcessConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        loadDomainObjectConfig(domainObjectTypeConfig);
    }

    @Override
    protected void postProcessConfig(DomainObjectTypeConfig config) {
        createAllConstraints(config);
    }

    private void createAclTables(Collection<DomainObjectTypeConfig> configList) {
        for (DomainObjectTypeConfig config : configList) {
            createAclTablesFor(config);
        }
    }
}
