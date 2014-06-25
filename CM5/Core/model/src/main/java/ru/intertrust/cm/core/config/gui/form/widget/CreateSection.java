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
public class CreateSection extends OperationConfig{

    @Attribute(name = "type", required = false)
    private String type;

    @ElementList(inline = true)
    private List<FieldValueConfig> activeFields = new ArrayList<FieldValueConfig>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FieldValueConfig> getActiveFields() {
        return activeFields;
    }

    public void setActiveFields(List<FieldValueConfig> activeFields) {
        this.activeFields = activeFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreateSection that = (CreateSection) o;

        if (activeFields != null ? !activeFields.equals(that.activeFields) : that.activeFields != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (activeFields != null ? activeFields.hashCode() : 0);
        return result;
    }
}
