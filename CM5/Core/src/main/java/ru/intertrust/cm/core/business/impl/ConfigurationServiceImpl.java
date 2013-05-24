package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.dao.api.DataStructureDAO;
import ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationService}
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:32 PM
 */
public class ConfigurationServiceImpl implements ConfigurationService {

    private DataStructureDAO dataStructureDAO;

    /**
     * Устанавливает  {@link #dataStructureDAO}
     * @param dataStructureDAO DataStructureDAO
     */
    public void setDataStructureDAO(DataStructureDAO dataStructureDAO) {
        this.dataStructureDAO = dataStructureDAO;
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.business.api.ConfigurationService#loadConfiguration(ru.intertrust.cm.core.config.Configuration)}
     * @param configuration конфигурация бизнес-объектов
     */
    @Override
    public void loadConfiguration(Configuration configuration) {
        if(isConfigurationLoaded()) {
            return;
        }

        dataStructureDAO.createServiceTables();

        RecursiveLoader recursiveLoader = new RecursiveLoader(configuration);
        recursiveLoader.load();
    }

    public void loadSystemObjectConfig(BusinessObjectConfig businessObjectConfig) {
        if(isSystemObjectLoaded(businessObjectConfig)) {
            return;
        }
        dataStructureDAO.createTable(businessObjectConfig, false);        
    }

    public boolean isSystemObjectLoaded(BusinessObjectConfig businessObjectConfig) {
        String tableName = DataStructureNamingHelper.getSqlName(businessObjectConfig);
        return dataStructureDAO.doesTableExists(tableName);        
    }

    private Boolean isConfigurationLoaded() {
        Integer tablesCount = dataStructureDAO.countTables();
        if(tablesCount == null) {
            throw new RuntimeException("Error occurred when calling DataStructureDAO for tables count");
        }

        return tablesCount > 0;
    }

    private class RecursiveLoader {
        private Configuration configuration;
        private Set<String> loadedBusinessObjectConfigs = new HashSet<>();

        private RecursiveLoader(Configuration configuration) {
            this.configuration = configuration;
        }

        private void load() {
            List<BusinessObjectConfig> businessObjectConfigs = configuration.getBusinessObjectConfigs();
            if(businessObjectConfigs.isEmpty())  {
                return;
            }

            dataStructureDAO.createServiceTables();

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

            dataStructureDAO.createTable(businessObjectConfig, true);
            loadedBusinessObjectConfigs.add(businessObjectConfig.getName()); // add to loaded configs set
        }

        private void loadDependentBusinessObjectConfigs(BusinessObjectConfig businessObjectConfig) {
            for(FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
                if((ReferenceFieldConfig.class.equals(fieldConfig.getClass()))) {
                    ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                    loadBusinessObjectConfig(ConfigurationHelper.findBusinessObjectConfigByName(configuration, referenceFieldConfig.getType()));
                }
            }
        }

    }

}
