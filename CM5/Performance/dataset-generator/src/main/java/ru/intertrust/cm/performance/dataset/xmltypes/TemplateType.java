
package ru.intertrust.cm.performance.dataset.xmltypes;

import org.simpleframework.xml.Element;


/**
 * <p>Java-класс для отображения комплексного xml-типа templateType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="templateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="object" type="{}objectType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class TemplateType {

    @Element
    protected ObjectType object;

    public ObjectType getObject() {
        return object;
    }

    public void setObject(ObjectType value) {
        this.object = value;
    }

}
