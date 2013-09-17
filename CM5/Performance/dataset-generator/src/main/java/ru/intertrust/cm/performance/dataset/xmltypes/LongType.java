

package ru.intertrust.cm.performance.dataset.xmltypes;

import org.simpleframework.xml.Attribute;


/**
 * <p>Java-класс для отображения комплексного xml-типа longType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="longType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="minValue" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="maxValue" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="noValueChance" type="{}doubleLimited" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class LongType extends FieldType {

    @Attribute(required = false)
    protected String name;
    @Attribute(required = false)
    protected Long value;
    @Attribute(required = false)
    protected Long minValue;
    @Attribute(required = false)
    protected Long maxValue;
    @Attribute(required = false)
    protected Double noValueChance;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getMinValue() {
        return minValue;
    }

    public void setMinValue(Long value) {
        this.minValue = value;
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long value) {
        this.maxValue = value;
    }

    public Double getNoValueChance() {
        return noValueChance;
    }

    public void setNoValueChance(Double value) {
        this.noValueChance = value;
    }

}
