package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbilyi on 18.06.2014.
 */

@Root(name = "update")
public class UpdateConfig extends OperationConfig{

    @ElementList(entry = "field", inline = true)
    private List<FieldValueConfig> fieldValueConfigs = new ArrayList<FieldValueConfig>();

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

        UpdateConfig that = (UpdateConfig) o;

        if (fieldValueConfigs != null ? !fieldValueConfigs.equals(that.fieldValueConfigs) : that.fieldValueConfigs != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return fieldValueConfigs != null ? fieldValueConfigs.hashCode() : 0;
    }
}
