package ru.intertrust.cm.test.configuration;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.FindObjectSettings;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FindPersonByDomainObjectFieldsSettings that = (FindPersonByDomainObjectFieldsSettings) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return field != null ? field.hashCode() : 0;
    }
}
