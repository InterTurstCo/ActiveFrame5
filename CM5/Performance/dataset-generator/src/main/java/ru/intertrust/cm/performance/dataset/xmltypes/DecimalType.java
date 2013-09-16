
package ru.intertrust.cm.performance.dataset.xmltypes;

import java.math.BigDecimal;

import org.simpleframework.xml.Attribute;


/**
 * <p>Java-класс для отображения комплексного xml-типа decimalType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="decimalType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="minValue" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="maxValue" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="noValueChance" type="{}doubleLimited" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

public class DecimalType extends FieldType {

    @Attribute(required = false)
    protected String name;
    @Attribute(required = false)
    protected BigDecimal value;
    @Attribute(required = false)
    protected BigDecimal minValue;
    @Attribute(required = false)
    protected BigDecimal maxValue;
    @Attribute(required = false)
    protected Double noValueChance;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal value) {
        this.minValue = value;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal value) {
        this.maxValue = value;
    }

    public Double getNoValueChance() {
        return noValueChance;
    }

    public void setNoValueChance(Double value) {
        this.noValueChance = value;
    }

}
