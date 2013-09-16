

package ru.intertrust.cm.performance.dataset.xmltypes;

import org.simpleframework.xml.Attribute;


/**
 * <p>Java-класс для отображения комплексного xml-типа stringType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="stringType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="minLength" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="maxLength" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="length" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="noValueChance" type="{}doubleLimited" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

public class StringType extends FieldType {

    @Attribute(required = false)
    protected String name;
    @Attribute(required = false)
    protected Integer minLength;
    @Attribute(required = false)
    protected Integer maxLength;
    @Attribute(required = false)
    protected Integer length;
    @Attribute(required = false)
    protected String value;
    @Attribute(required = false)
    protected Double noValueChance;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer value) {
        this.minLength = value;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer value) {
        this.maxLength = value;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer value) {
        this.length = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Double getNoValueChance() {
        return noValueChance;
    }

    public void setNoValueChance(Double value) {
        this.noValueChance = value;
    }

}
