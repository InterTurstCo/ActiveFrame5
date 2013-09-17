

package ru.intertrust.cm.performance.dataset.xmltypes;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;


/**
 * <p>Java-класс для отображения комплексного xml-типа setType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="setType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="object" type="{}objectType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="quantity" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="minQuantity" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="maxQuantity" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="averageQuantity" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class SetType {

    @Element(required = true)
    protected ObjectType object;
    @Attribute(required = false)
    protected String id;
    @Attribute(required = false)
    protected Integer quantity;
    @Attribute(required = false)
    protected Integer minQuantity;
    @Attribute(required = false)
    protected Integer maxQuantity;
    @Attribute(required = false)
    protected Integer averageQuantity;

    public ObjectType getObject() {
        return object;
    }

    public void setObject(ObjectType value) {
        this.object = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer value) {
        this.quantity = value;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer value) {
        this.minQuantity = value;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer value) {
        this.maxQuantity = value;
    }

    public Integer getAverageQuantity() {
        return averageQuantity;
    }

    public void setAverageQuantity(Integer value) {
        this.averageQuantity = value;
    }

}
