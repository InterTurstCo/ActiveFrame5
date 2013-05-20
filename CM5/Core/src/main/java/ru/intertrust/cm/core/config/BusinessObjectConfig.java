package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/1/13
 *         Time: 8:50 PM
 */
@Root(name = "businessObject")
public class BusinessObjectConfig {
    @Attribute(required = true)
    private String name;

    @Attribute(name = "extends", required = false)
    private String parentConfig;

    // we can't use a list here directly, as elements inside are different, that's why such a "trick"
    @Element(name = "fields")
    private BusinessObjectFieldsConfig businessObjectFieldsConfig;

    @ElementList(entry="uniqueKey", type=UniqueKeyConfig.class, inline=true, required = false)
    private List<UniqueKeyConfig> uniqueKeyConfigs;

    public BusinessObjectConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentConfig() {
        return parentConfig;
    }

    public void setParentConfig(String parentConfig) {
        this.parentConfig = parentConfig;
    }

    public BusinessObjectFieldsConfig getBusinessObjectFieldsConfig() {
        return businessObjectFieldsConfig;
    }

    public void setBusinessObjectFieldsConfig(BusinessObjectFieldsConfig businessObjectFieldsConfig) {
        this.businessObjectFieldsConfig = businessObjectFieldsConfig;
    }

    public List<FieldConfig> getFieldConfigs() {
        if(businessObjectFieldsConfig == null || businessObjectFieldsConfig.getFieldConfigs() == null) {
            return Collections.emptyList();
        }
        return businessObjectFieldsConfig.getFieldConfigs();
    }

    public List<UniqueKeyConfig> getUniqueKeyConfigs() {
        if(uniqueKeyConfigs == null) {
            uniqueKeyConfigs = new ArrayList<UniqueKeyConfig>();
        }
        return uniqueKeyConfigs;
    }

    public void setUniqueKeyConfigs(List<UniqueKeyConfig> uniqueKeyConfigs) {
        this.uniqueKeyConfigs = uniqueKeyConfigs;
    }
}
