
package ru.intertrust.cm.performance.dataset.xmltypes;

import java.util.Date;

import javax.xml.datatype.Duration;

import org.simpleframework.xml.Attribute;


/**
 * <p>Java-класс для отображения комплексного xml-типа dateTimeType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="dateTimeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="minValue" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="maxValue" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="relativeValue" type="{http://www.w3.org/2001/XMLSchema}duration" />
 *       &lt;attribute name="relativeMinValue" type="{http://www.w3.org/2001/XMLSchema}duration" />
 *       &lt;attribute name="relativeMaxValue" type="{http://www.w3.org/2001/XMLSchema}duration" />
 *       &lt;attribute name="noValueChance" type="{}doubleLimited" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

public class DateTimeType extends FieldType {

    @Attribute(required = false)
    protected String name;
    @Attribute(required = false)
    protected Date value;
    @Attribute(required = false)
    protected Date minValue;
    @Attribute(required = false)
    protected Date maxValue;
    @Attribute(required = false)
    protected Duration relativeValue;
    @Attribute(required = false)
    protected Duration relativeMinValue;
    @Attribute(required = false)
    protected Duration relativeMaxValue;
    @Attribute(required = false)
    protected Double noValueChance;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    public Date getMinValue() {
        return minValue;
    }

    public void setMinValue(Date value) {
        this.minValue = value;
    }

    public Date getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Date value) {
        this.maxValue = value;
    }

    public Duration getRelativeValue() {
        return relativeValue;
    }

    public void setRelativeValue(Duration value) {
        this.relativeValue = value;
    }

    public Duration getRelativeMinValue() {
        return relativeMinValue;
    }

    public void setRelativeMinValue(Duration value) {
        this.relativeMinValue = value;
    }

    public Duration getRelativeMaxValue() {
        return relativeMaxValue;
    }

    public void setRelativeMaxValue(Duration value) {
        this.relativeMaxValue = value;
    }

    public Double getNoValueChance() {
        return noValueChance;
    }

    public void setNoValueChance(Double value) {
        this.noValueChance = value;
    }
    
}
