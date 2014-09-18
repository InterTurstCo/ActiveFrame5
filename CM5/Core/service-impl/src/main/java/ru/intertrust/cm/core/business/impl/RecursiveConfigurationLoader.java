package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DataStructureDao;

import java.util.Collection;

/**
* Recursively loads configuration
*/
class RecursiveConfigurationLoader extends AbstractRecursiveConfigurationLoader {

    public RecursiveConfigurationLoader(DataStructureDao dataStructureDao) {
        setDataStructureDao(dataStructureDao);
    }

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
            if (!config.isTemplate()) {
                createAclTablesFor(config);
            }
        }
    }
}
