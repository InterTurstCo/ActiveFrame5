package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by andrey on 08.12.14.
 */
public class ColumnActionConfig implements Dto {
    @Attribute(name = "component-name",required = false)
    private String componentName;
    @Attribute(name = "access-checker",required = false)
    private String accessChecker;
    @Attribute(name = "new-objects-access-checker",required = false)
    private String newObjectsAccessChecker;
    @Element(name = "display", required = false)
    private ColumnDisplayConfig columnDisplayConfig;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getAccessChecker() {
        return accessChecker;
    }

    public void setAccessChecker(String accessChecker) {
        this.accessChecker = accessChecker;
    }

    public String getNewObjectsAccessChecker() {
        return newObjectsAccessChecker;
    }

    public void setNewObjectsAccessChecker(String newObjectsAccessChecker) {
        this.newObjectsAccessChecker = newObjectsAccessChecker;
    }

    public ColumnDisplayConfig getColumnDisplayConfig() {
        return columnDisplayConfig;
    }

    public void setColumnDisplayConfig(ColumnDisplayConfig columnDisplayConfig) {
        this.columnDisplayConfig = columnDisplayConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnActionConfig that = (ColumnActionConfig) o;

        if (accessChecker != null ? !accessChecker.equals(that.accessChecker) : that.accessChecker != null)
            return false;
        if (columnDisplayConfig != null ? !columnDisplayConfig.equals(that.columnDisplayConfig) : that.columnDisplayConfig != null)
            return false;
        if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null)
            return false;
        if (newObjectsAccessChecker != null ? !newObjectsAccessChecker.equals(that.newObjectsAccessChecker) : that.newObjectsAccessChecker != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = componentName != null ? componentName.hashCode() : 0;
        result = 31 * result + (accessChecker != null ? accessChecker.hashCode() : 0);
        result = 31 * result + (newObjectsAccessChecker != null ? newObjectsAccessChecker.hashCode() : 0);
        result = 31 * result + (columnDisplayConfig != null ? columnDisplayConfig.hashCode() : 0);
        return result;
    }
}
