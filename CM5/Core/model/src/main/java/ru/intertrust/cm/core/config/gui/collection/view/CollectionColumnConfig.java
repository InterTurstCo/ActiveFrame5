package ru.intertrust.cm.core.config.gui.collection.view;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 *
 * @author atsvetkov
 *
 */

public class CollectionColumnConfig implements Dto {

    @Attribute(required = true)
    private String field;

    @Attribute(required = true)
    private String name;

    @Attribute(required = false)
    private boolean hidden;

    @Attribute(required = false)
    private boolean sortable;

    @Attribute(required = false)
    private boolean editable;

    @Attribute(required = true)
    private String type;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
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

        CollectionColumnConfig that = (CollectionColumnConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (field != null ? !field.equals(that.field) : that.field != null) {
            return false;
        }
        if (editable != that.editable) {
            return false;
        }
        if (hidden != that.hidden) {
            return false;
        }
        if (sortable != that.sortable) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
