package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:12 AM
 */
public class DecimalFieldConfig extends FieldConfig {
    @Attribute
    private Integer precision; // total number of digits, for instance 12.34567 - precision is 7, scale is 5

    @Attribute
    private Integer scale;

    public DecimalFieldConfig() {
    }

    public Integer getPrecision() {

        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }
}
