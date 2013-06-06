package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionFilterReference {
    
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
