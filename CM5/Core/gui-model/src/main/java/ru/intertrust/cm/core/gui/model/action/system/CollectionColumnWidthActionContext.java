package ru.intertrust.cm.core.gui.model.action.system;

/**
 * @author Sergey.Okolot
 *         Created on 28.07.2014 16:09.
 */
public class CollectionColumnWidthActionContext extends AbstractUserSettingActionContext {
    public static final String COMPONENT_NAME = "collection.column.width.action";

    private String field;
    private String width;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }
}
