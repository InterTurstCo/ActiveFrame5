package ru.intertrust.cm.core.gui.model.action.system;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey.Okolot
 *         Created on 28.07.2014 16:09.
 */
public class CollectionColumnWidthActionContext extends AbstractUserSettingActionContext {
    public static final String COMPONENT_NAME = "collection.column.width.action";

    private String field;
    private String width;
    private Map<String, String> fieldWidthMap;

    @Deprecated
    /*
    use fieldWidthMap instead
     */
    public String getField() {
        return field;
    }

    @Deprecated
     /*
    use fieldWidthMap instead
    */
    public void setField(String field) {
        this.field = field;
    }

    @Deprecated
    /*
    use fieldWidthMap instead
    */
    public String getWidth() {
        return width;
    }
    @Deprecated
    /*
    use fieldWidthMap instead
    */
    public void setWidth(String width) {
        this.width = width;
    }

    public Map<String, String> getFieldWidthMap() {
        return fieldWidthMap == null ? new HashMap<String, String>(0) : fieldWidthMap;
    }

    public void setFieldWidthMap(Map<String, String> fieldWidthMap) {
        this.fieldWidthMap = fieldWidthMap;
    }
}
