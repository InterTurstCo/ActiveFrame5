package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:09 AM
 */
public class StringFieldConfig extends FieldConfig {
    @Attribute(required = true)
    private int length;

    public StringFieldConfig() {
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
