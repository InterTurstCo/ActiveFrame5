package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.FieldValueConfig;

/**
 * Created by andrey on 18.09.14.
 */

public class DefaultValueConfig implements Dto {

    public DefaultValueConfig() {
    }

    @Element(name = "field")
    private FieldValueConfig fieldValueConfig;

    public FieldValueConfig getFieldValueConfig() {
        return fieldValueConfig;
    }

    public void setFieldValueConfig(FieldValueConfig fieldValueConfig) {
        System.out.println("field value = " + fieldValueConfig);
        this.fieldValueConfig = fieldValueConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultValueConfig that = (DefaultValueConfig) o;
        if (fieldValueConfig != null ? !fieldValueConfig.equals(that.fieldValueConfig) : that.fieldValueConfig != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return fieldValueConfig != null ? fieldValueConfig.hashCode() : 0;
    }
}
