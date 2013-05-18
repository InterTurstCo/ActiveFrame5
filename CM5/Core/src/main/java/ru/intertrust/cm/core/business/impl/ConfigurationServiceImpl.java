package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDAO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public class ConfigurationServiceImpl implements ConfigurationService {

    private DataStructureDAO dataStructureDAO;

    public void setDataStructureDAO(DataStructureDAO dataStructureDAO) {
        this.dataStructureDAO = dataStructureDAO;
    }

    @Override
    public void loadConfiguration(Configuration configuration) {
        if(isConfigurationLoaded()) {
            return;
        }

        ConfigurationLoader configurationLoader = new ConfigurationLoader(configuration);
        configurationLoader.load();
    }

    private Boolean isConfigurationLoaded() {
        Integer tablesCount = dataStructureDAO.countTables();
        if(tablesCount == null) {
            throw new RuntimeException("Error occurred when calling DataStructureDAO for tables count");
        }

        return tablesCount > 0;
    }

    private class ConfigurationLoader {
        private Configuration configuration;
        private Set<String> loadedBusinessObjectConfigs = new HashSet<String>();

        private ConfigurationLoader(Configuration configuration) {
            this.configuration = configuration;
        }

        public void load() {
            List<BusinessObjectConfig> businessObjectConfigs = configuration.getBusinessObjectConfigs();
            if(businessObjectConfigs.isEmpty())  {
                return;
            }

            for(BusinessObjectConfig businessObjectConfig : businessObjectConfigs) {
                loadBusinessObjectConfig(businessObjectConfig);
            }
        }

        private void loadBusinessObjectConfig(BusinessObjectConfig businessObjectConfig) {
            if(loadedBusinessObjectConfigs.contains(businessObjectConfig.getName())) { // skip if already loaded
                return;
            }

            // First load referenced business object configurations
            loadDependentBusinessObjectConfigs(businessObjectConfig);

            dataStructureDAO.createTable(businessObjectConfig);
            loadedBusinessObjectConfigs.add(businessObjectConfig.getName()); // add to loaded configs set
        }

        private void loadDependentBusinessObjectConfigs(BusinessObjectConfig businessObjectConfig) {
            for(FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
                if((ReferenceFieldConfig.class.equals(fieldConfig.getClass()))) {
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    loadBusinessObjectConfig(configuration.findBusinessObjectConfigByName(referenceFieldConfig.getType()));
                }
            }
        }

    }

}
