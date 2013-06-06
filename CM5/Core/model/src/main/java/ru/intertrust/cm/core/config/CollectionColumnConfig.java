package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionColumnConfig {

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
    private int sequence;

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

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

}
