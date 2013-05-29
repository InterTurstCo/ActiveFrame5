package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:12 AM
 */
public class ReferenceFieldConfig extends FieldConfig {
    @Attribute
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
