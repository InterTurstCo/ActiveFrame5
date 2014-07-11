package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbilyi on 19.06.2014.
 */

@Root(name = "unique-key-value")
public class UniqueKeyValueConfig implements Dto{

    @ElementList(inline = true)
    private List<FieldValueConfig> fieldValueConfigs = new ArrayList<FieldValueConfig>();

    @Attribute(name = "type", required = false)
    private String type;

    public List<FieldValueConfig> getFieldValueConfigs() {
        return fieldValueConfigs;
    }

    public void setFieldValueConfigs(List<FieldValueConfig> fieldValueConfigs) {
        this.fieldValueConfigs = fieldValueConfigs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UniqueKeyValueConfig that = (UniqueKeyValueConfig) o;

        if (fieldValueConfigs != null ? !fieldValueConfigs.equals(that.fieldValueConfigs) : that.fieldValueConfigs != null) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
