package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.base.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author atsvetkov
 *
 */
public class DomainObjectTypeUtility {

    public static List<FieldConfig> getAllFieldConfigs(DomainObjectFieldsConfig objectFieldsConfig,
                                                       ConfigurationExplorer configurationExplorer) {
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

    public static List<FieldConfig> getAllFieldConfigsIncludingInherited(DomainObjectTypeConfig domainObjectTypeConfig,
                                                       ConfigurationExplorer configurationExplorer) {
        final DomainObjectFieldsConfig objectFieldsConfig = domainObjectTypeConfig.getDomainObjectFieldsConfig();
        final List<FieldConfig> fieldConfigs = getAllFieldConfigs(objectFieldsConfig, configurationExplorer);
        final String extendedType = domainObjectTypeConfig.getExtendsAttribute();
        if (extendedType != null) {
            fieldConfigs.addAll(getAllFieldConfigsIncludingInherited(configurationExplorer.getDomainObjectTypeConfig(extendedType), configurationExplorer));
        }
        return fieldConfigs;
    }

    public static boolean isParentObject(DomainObjectTypeConfig config, ConfigurationExplorer configurationExplorer) {
        boolean isParent = false;
        if (configurationExplorer.isAuditLogType(config.getName())) {
            DomainObjectTypeConfig parentObjectConfig = getSourceDomainObjectType(config, configurationExplorer);
            if (parentObjectConfig != null && parentObjectConfig.getExtendsAttribute() != null && (!parentObjectConfig.isTemplate())) {
                isParent = false;
            } else {
                isParent = true;
            }

        } else {
            isParent = config.getExtendsAttribute() == null;
        }
        return isParent;
    }

    public static DomainObjectTypeConfig getSourceDomainObjectType(DomainObjectTypeConfig config, ConfigurationExplorer configurationExplorer) {
        if (!configurationExplorer.isAuditLogType(config.getName())) {
            return null;
        }

        String name = config.getName().replace(Configuration.AUDIT_LOG_SUFFIX, "");
        return configurationExplorer.getDomainObjectTypeConfig(name);
    }
    
}
