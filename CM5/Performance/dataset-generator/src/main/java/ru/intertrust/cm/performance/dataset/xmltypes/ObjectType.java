

package ru.intertrust.cm.performance.dataset.xmltypes;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;


/**
 * <p>Java-класс для отображения комплексного xml-типа objectType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="objectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="string" type="{}stringType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="dateTime" type="{}dateTimeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="reference" type="{}referenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="children" type="{}childrenType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="long" type="{}longType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="decimal" type="{}decimalType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ObjectType {

    @ElementListUnion({
        @ElementList(entry = "string", type = StringType.class, inline = true, required = false),
        @ElementList(entry = "dateTime", type = DateTimeType.class, inline = true, required = false),
        @ElementList(entry = "reference", type = ReferenceType.class, inline = true, required = false),
        @ElementList(entry = "children", type = ChildrenType.class, inline = true, required = false),
        @ElementList(entry = "long", type = LongType.class, inline = true, required = false),
        @ElementList(entry = "decimal", type = DecimalType.class, inline = true, required = false)
    })
    protected List<FieldType> stringOrDateTimeOrReference;
    @Attribute(required = false)
    protected String type;

    public List<FieldType> getStringOrDateTimeOrReference() {
        if (stringOrDateTimeOrReference == null) {
            stringOrDateTimeOrReference = new ArrayList<FieldType>();
        }
        return this.stringOrDateTimeOrReference;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

}
