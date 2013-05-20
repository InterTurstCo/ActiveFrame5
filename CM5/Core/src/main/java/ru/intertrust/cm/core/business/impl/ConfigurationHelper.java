package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.config.BusinessObjectConfig;
import ru.intertrust.cm.core.config.Configuration;
import ru.intertrust.cm.core.config.FieldConfig;

/**
 * @author vmatsukevich
 *         Date: 5/18/13
 *         Time: 4:03 PM
 */
public class ConfigurationHelper {

    public static BusinessObjectConfig findBusinessObjectConfigByName(Configuration configuration, String name) {
        for(BusinessObjectConfig businessObjectConfig : configuration.getBusinessObjectConfigs()) {
            if(businessObjectConfig.getName().equals(name)) {
                return businessObjectConfig;
            }
        }
        throw new RuntimeException("BusinessObjectConfiguration is not found for name '" + name + "'");
    }

    public static FieldConfig findFieldConfigForBusinessObject(BusinessObjectConfig businessObjectConfig, String fieldName) {
        for(FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
            if(fieldName.equals(fieldConfig.getName())) {
                return fieldConfig;
            }
        }
        throw new RuntimeException("FieldConfig with name " + fieldName + " is not found in business object '" + businessObjectConfig.getName() + "'");
    }
}
