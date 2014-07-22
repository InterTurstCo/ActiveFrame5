package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


/**
 * 
 * @author atsvetkov
 *
 */
@Root(name ="domain-object-field-addressee")
public class FindPersonByDomainObjectFieldsSettings implements FindObjectSettings {

    @Attribute(name = "field-name", required = true)
    private String field;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

}
