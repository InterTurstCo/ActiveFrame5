package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbilyi on 18.06.2014.
 */

@Root(name = "update")
public class UpdateSection extends OperationConfig{

    @ElementList(inline = true)
    private List<FieldValueConfig> activeFields = new ArrayList<FieldValueConfig>();

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

        UpdateSection that = (UpdateSection) o;

        if (activeFields != null ? !activeFields.equals(that.activeFields) : that.activeFields != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return activeFields != null ? activeFields.hashCode() : 0;
    }
}
