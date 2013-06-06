package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionFilterCriteria {

    @Attribute(required = true)
    private String placeholder;

    @Attribute(required = false)
    private String condition;

    @Text(data = true)
    private String value;

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
