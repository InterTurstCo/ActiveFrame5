package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author atsvetkov
 *
 */
public class DomainObjectTypeUtility {

    public static List<FieldConfig> getAllFieldConfigs(DomainObjectFieldsConfig objectFieldsConfig, ConfigurationExplorer configurationExplorer) {
        List<FieldConfig> totalFieldConfigs = new ArrayList<>();
        if (objectFieldsConfig == null || objectFieldsConfig.getFieldConfigs() == null) {
            return totalFieldConfigs;
        }
        totalFieldConfigs.addAll(objectFieldsConfig.getFieldConfigs());

        List<IncludeFieldGroupConfig> includeGroupConfigs = objectFieldsConfig.getIncludeGroups();

        for (IncludeFieldGroupConfig includeGroup : includeGroupConfigs) {
            String groupName = includeGroup.getName();            
            DomainObjectFieldGroupConfig fieldGroupConfig = configurationExplorer.getConfig(DomainObjectFieldGroupConfig.class, groupName);
            totalFieldConfigs.addAll(getAllFieldConfigs(fieldGroupConfig, configurationExplorer));
        }

        return totalFieldConfigs;
    }
    
}
