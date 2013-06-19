package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

import java.io.Serializable;

/**
 *
 * @author atsvetkov
 *
 */
public class CollectionFilterCriteriaConfig implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionFilterCriteriaConfig that = (CollectionFilterCriteriaConfig) o;

        if (condition != null ? !condition.equals(that.condition) : that.condition != null) return false;
        if (placeholder != null ? !placeholder.equals(that.placeholder) : that.placeholder != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = placeholder != null ? placeholder.hashCode() : 0;
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
