package ru.intertrust.cm.core.config;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.StringWriter;

/**
 * @author Denis Mitavskiy
 *         Date: 5/1/13
 *         Time: 8:50 PM
 */
@Root(name = "businessObject")
public class BusinessObjectConfig {
    @Attribute
    private String name;

    // we can't use a list here directly, as elements inside are different, that's why such a "trick"
    @Element(name = "fields")
    private BusinessObjectFieldsConfig businessObjectFieldsConfig;

    public BusinessObjectConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BusinessObjectFieldsConfig getBusinessObjectFieldsConfig() {
        return businessObjectFieldsConfig;
    }

    public void setBusinessObjectFieldsConfig(BusinessObjectFieldsConfig businessObjectFieldsConfig) {
        this.businessObjectFieldsConfig = businessObjectFieldsConfig;
    }
}
