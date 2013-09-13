package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "integer-box")
public class IntegerBoxConfig implements Dto {
    @Attribute(name = "id")
    private String id;

    @Element(name = "field-path")
    private FieldPathConfig fieldPathConfig;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FieldPathConfig getFieldPathConfig() {
        return fieldPathConfig;
    }

    public void setFieldPathConfig(FieldPathConfig fieldPathConfig) {
        this.fieldPathConfig = fieldPathConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntegerBoxConfig that = (IntegerBoxConfig) o;

        if (fieldPathConfig != null ? !fieldPathConfig.equals(that.fieldPathConfig) : that.fieldPathConfig != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (fieldPathConfig != null ? fieldPathConfig.hashCode() : 0);
        return result;
    }
}
