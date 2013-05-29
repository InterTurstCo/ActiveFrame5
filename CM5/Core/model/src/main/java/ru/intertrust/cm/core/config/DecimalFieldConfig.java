package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:12 AM
 */
public class DecimalFieldConfig extends FieldConfig {
    @Attribute
    private int precision; // total number of digits, for instance 12.34567 - precision is 7, scale is 5

    @Attribute
    private int scale;

    public DecimalFieldConfig() {
    }

    public int getPrecision() {

        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
}
