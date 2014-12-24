package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 05.12.14.
 */
public class SummaryTableActionColumnConfig implements Dto {
    @Attribute(name = "type", required = false)
    private String type;
    @Attribute(name = "tooltip", required = false)
    private String tooltip;
    @Attribute(name = "component-name", required = false)
    private String componentName;
    @Attribute(name = "access-checker", required = false)
    private String accessChecker;
    @Attribute(name = "new-objects-access-checker", required = false)
    private String newObjectsAccessChecker;
    @Element(name = "display", required = false)
    private ColumnDisplayConfig columnDisplayConfig;

    @ElementList(name = "action", entry = "action", inline = true, required = false)
    private List<ColumnActionConfig> columnActionConfig = new ArrayList<>();

    public List<ColumnActionConfig> getColumnActionConfig() {
        return columnActionConfig;
    }

    public void setColumnActionConfig(List<ColumnActionConfig> columnActionConfig) {
        this.columnActionConfig = columnActionConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SummaryTableActionColumnConfig that = (SummaryTableActionColumnConfig) o;

        if (columnActionConfig != null ? !columnActionConfig.equals(that.columnActionConfig) : that.columnActionConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return columnActionConfig != null ? columnActionConfig.hashCode() : 0;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }
}
