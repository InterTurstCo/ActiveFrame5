
package ru.intertrust.cm.performance.dataset.xmltypes;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;


/**
 * <p>Java-класс для отображения комплексного xml-типа referenceType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="referenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="object" type="{}objectType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="parentReference" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="setId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="treeReference" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="treeDepth" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="minTreeDepth" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="maxTreeDepth" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="averageTreeDepth" type="{}doubleLimited" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

public class ReferenceType extends FieldType {
    
    @Element(required = false)
    protected ObjectType object;
    @Attribute(required = false)
    protected String name;
    @Attribute(required = false)
    protected Boolean parentReference;
    @Attribute(required = false)
    protected String type;
    @Attribute(required = false)
    protected String setId;
    @Attribute(required = false)
    protected Boolean treeReference;
    @Attribute(required = false)
    protected Integer treeDepth;
    @Attribute(required = false)
    protected Integer minTreeDepth;
    @Attribute(required = false)
    protected Integer maxTreeDepth;
    @Attribute(required = false)
    protected Double averageTreeDepth;

    public ObjectType getObject() {
        return object;
    }

    public void setObject(ObjectType value) {
        this.object = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Boolean isParentReference() {
        return parentReference;
    }

    public void setParentReference(Boolean value) {
        this.parentReference = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String value) {
        this.setId = value;
    }

    public Boolean isTreeReference() {
        return treeReference;
    }

    public void setTreeReference(Boolean value) {
        this.treeReference = value;
    }

    public Integer getTreeDepth() {
        return treeDepth;
    }

    public void setTreeDepth(Integer value) {
        this.treeDepth = value;
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

    public Double getAverageTreeDepth() {
        return averageTreeDepth;
    }

    public void setAverageTreeDepth(Double value) {
        this.averageTreeDepth = value;
    }

}
