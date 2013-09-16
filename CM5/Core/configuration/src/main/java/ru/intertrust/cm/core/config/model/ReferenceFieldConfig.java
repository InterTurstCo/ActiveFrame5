package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.FieldType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 5/2/13
 *         Time: 11:12 AM
 */
public class ReferenceFieldConfig extends FieldConfig {

    @ElementList(entry="type", inline=true)
    private List<ReferenceFieldTypeConfig> types = new ArrayList<>();

    public List<ReferenceFieldTypeConfig> getTypes() {
        return types;
    }

    public void setTypes(List<ReferenceFieldTypeConfig> types) {
        if(types != null) {
            this.types = types;
        } else {
            this.types.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ReferenceFieldConfig that = (ReferenceFieldConfig) o;

        if (types != null ? !types.equals(that.types) : that.types != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (types != null ? types.hashCode() : 0);
        return result;
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.REFERENCE;
    }
}
