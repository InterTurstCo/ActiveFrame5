package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrey on 18.12.14.
 */
public class ColumnContext {
    private Id objectId;
    private String value;
    private String accessChecker;
    private String newObjectsAccessChecker;
    private String componentName;

    private Map<String, ColumnContext> nestedContexts = new HashMap<>();
    private RowItem rowItem;

    public String renderRow() {
        return value;
    }

    public void setObjectId(Id objectId) {
        this.objectId = objectId;
    }

    public Id getObjectId() {
        return objectId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setAccessChecker(String accessChecker) {
        this.accessChecker = accessChecker;
    }

    public String getAccessChecker() {
        return accessChecker;
    }

    public void setNewObjectsAccessChecker(String newObjectsAccessChecker) {
        this.newObjectsAccessChecker = newObjectsAccessChecker;
    }

    public String getNewObjectsAccessChecker() {
        return newObjectsAccessChecker;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setNestedColumnContext(String componentName, ColumnContext nestedContext) {
        nestedContexts.put(componentName,nestedContext);
    }

    public void setRowItem(RowItem rowItem) {
        this.rowItem = rowItem;
    }

    public RowItem getRowItem() {
        return rowItem;
    }
}
