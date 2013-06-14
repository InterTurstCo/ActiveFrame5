package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

import java.io.Serializable;

/**
 *
 * @author atsvetkov
 *
 */
public class CollectionFilterReferenceConfig implements Serializable {

    @Attribute(required = true)
    private String placeholder;

    @Text(data = true)
    private String value;

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
