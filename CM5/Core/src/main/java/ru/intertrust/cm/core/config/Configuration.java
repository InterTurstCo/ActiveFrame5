package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 12:05 PM
 */
@Root
public class Configuration {
    @ElementList(inline = true)
    private List<BusinessObjectConfig> businessObjectConfigs;

    public List<BusinessObjectConfig> getBusinessObjectConfigs() {
        if(businessObjectConfigs == null) {
            businessObjectConfigs = new ArrayList<BusinessObjectConfig>();
        }
        return businessObjectConfigs;
    }

    public void setBusinessObjectConfigs(List<BusinessObjectConfig> businessObjectConfigs) {
        this.businessObjectConfigs = businessObjectConfigs;
    }

    public BusinessObjectConfig findBusinessObjectConfigByName(String name) {
        for(BusinessObjectConfig businessObjectConfig : getBusinessObjectConfigs()) {
            if(businessObjectConfig.getName().equals(name)) {
                return businessObjectConfig;
            }
        }
        throw new IllegalStateException("BusinessObjectConfiguration is not found for name '" + name + "'");
    }
    
    public static FieldConfig findFieldConfigForBusisnessObject(BusinessObjectConfig businessObjectConfig, String fieldName) {
        for(FieldConfig fieldConfig : businessObjectConfig.getFieldConfigs()) {
            if(fieldName.equals(fieldConfig.getName())) {
                return fieldConfig;
            }
        }
        throw new IllegalStateException("FieldConfig with name " + fieldName + " is not found in business object '" + businessObjectConfig.getName() + "'");        
    }
}
