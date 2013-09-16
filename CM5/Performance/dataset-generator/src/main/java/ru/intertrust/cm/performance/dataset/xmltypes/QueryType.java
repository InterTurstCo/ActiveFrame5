
package ru.intertrust.cm.performance.dataset.xmltypes;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;


/**
 * <p>Java-класс для отображения комплексного xml-типа queryType.
 * 
 * <p>Ниже преведено определение этого типа в xml-схеме.
 * 
 * <pre>
 * &lt;complexType name="queryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="template" type="{}templateType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="set" type="{}setType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@Root(name="query")
public class QueryType {
    
    @ElementList(entry="template", required = false, inline = true)
    protected List<TemplateType> template;
    @ElementList(entry="set", required = true, inline = true)
    protected List<SetType> set;

    public List<TemplateType> getTemplate() {
        if (template == null) {
            template = new ArrayList<TemplateType>();
        }
        return this.template;
    }

    public List<SetType> getSet() {
        if (set == null) {
            set = new ArrayList<SetType>();
        }
        return this.set;
    }

}
