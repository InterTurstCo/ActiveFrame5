package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbilyi on 19.06.2014.
 */
@Root(name = "create")
public class CreateConfig extends OperationConfig{

    @Attribute(name = "type", required = false)
    private String type;

    @ElementList(inline = true)
    private List<FieldValueConfig> fieldValueConfigs = new ArrayList<FieldValueConfig>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FieldValueConfig> getFieldValueConfigs() {
        return fieldValueConfigs;
    }

    public void setFieldValueConfigs(List<FieldValueConfig> fieldValueConfigs) {
        this.fieldValueConfigs = fieldValueConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreateConfig that = (CreateConfig) o;

        if (fieldValueConfigs != null ? !fieldValueConfigs.equals(that.fieldValueConfigs) : that.fieldValueConfigs != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (fieldValueConfigs != null ? fieldValueConfigs.hashCode() : 0);
        return result;
    }
}
