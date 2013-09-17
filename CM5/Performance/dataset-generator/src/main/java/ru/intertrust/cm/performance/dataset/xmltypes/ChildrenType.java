
package ru.intertrust.cm.performance.dataset.xmltypes;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;


/**
 * <p>Java-класс для отображения комплексного xml-типа childrenType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="childrenType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="object" type="{}objectType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="minTreeDepth" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="maxTreeDepth" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="treeReference" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="treeDepth" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="quantity" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="minQuantity" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="maxQuantity" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="averageQuantity" type="{}doubleLimited" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ChildrenType extends FieldType {
    
    @Element(required = false)
    protected ObjectType object;
    @Attribute(required = false)
    protected Integer minTreeDepth;
    @Attribute(required = false)
    protected Integer maxTreeDepth;
    @Attribute(required = false)
    protected Integer treeReference;
    @Attribute(required = false)
    protected Integer treeDepth;
    @Attribute(required = false)
    protected Integer quantity;
    @Attribute(required = false)
    protected Integer minQuantity;
    @Attribute(required = false)
    protected Integer maxQuantity;
    @Attribute(required = false)
    protected Double averageQuantity;

    public ObjectType getObject() {
        return object;
    }

    public void setObject(ObjectType value) {
        this.object = value;
    }

    public Integer getMinTreeDepth() {
        return minTreeDepth;
    }

    public void setMinTreeDepth(Integer value) {
        this.minTreeDepth = value;
    }

    public Integer getMaxTreeDepth() {
        return maxTreeDepth;
    }

    public void setMaxTreeDepth(Integer value) {
        this.maxTreeDepth = value;
    }

    public Integer getTreeReference() {
        return treeReference;
    }

    public void setTreeReference(Integer value) {
        this.treeReference = value;
    }

    public Integer getTreeDepth() {
        return treeDepth;
    }

    public void setTreeDepth(Integer value) {
        this.treeDepth = value;
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

    public Double getAverageQuantity() {
        return averageQuantity;
    }

    public void setAverageQuantity(Double value) {
        this.averageQuantity = value;
    }

}
