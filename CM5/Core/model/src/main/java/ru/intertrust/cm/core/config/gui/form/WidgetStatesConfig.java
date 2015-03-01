package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.02.2015
 *         Time: 10:37
 */
@Root(name = "widget-states")
public class WidgetStatesConfig implements Dto {
    @Element(name = "all", required = false)
    private AllWidgetsIndicationConfig allWidgetsIndicationConfig;

    @Element(name = "editable", required = false)
    private EditableWidgetsIndicationConfig editableWidgetsIndicationConfig;

    @Element(name = "widgets", required = false)
    private WidgetsIndicationConfig widgetsIndicationConfig;

    public AllWidgetsIndicationConfig getAllWidgetsIndicationConfig() {
        return allWidgetsIndicationConfig;
    }

    public void setAllWidgetsIndicationConfig(AllWidgetsIndicationConfig allWidgetsIndicationConfig) {
        this.allWidgetsIndicationConfig = allWidgetsIndicationConfig;
    }

    public EditableWidgetsIndicationConfig getEditableWidgetsIndicationConfig() {
        return editableWidgetsIndicationConfig;
    }

    public void setEditableWidgetsIndicationConfig(EditableWidgetsIndicationConfig editableWidgetsIndicationConfig) {
        this.editableWidgetsIndicationConfig = editableWidgetsIndicationConfig;
    }

    public WidgetsIndicationConfig getWidgetsIndicationConfig() {
        return widgetsIndicationConfig;
    }

    public void setWidgetsIndicationConfig(WidgetsIndicationConfig widgetsIndicationConfig) {
        this.widgetsIndicationConfig = widgetsIndicationConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetStatesConfig that = (WidgetStatesConfig) o;

        if (allWidgetsIndicationConfig != null ? !allWidgetsIndicationConfig.equals(that.allWidgetsIndicationConfig)
                : that.allWidgetsIndicationConfig != null){
            return false;
        }
        if (editableWidgetsIndicationConfig != null ? !editableWidgetsIndicationConfig.equals(that.editableWidgetsIndicationConfig)
                : that.editableWidgetsIndicationConfig != null) {
            return false;
        }
        if (widgetsIndicationConfig != null ? !widgetsIndicationConfig.equals(that.widgetsIndicationConfig)
                : that.widgetsIndicationConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = allWidgetsIndicationConfig != null ? allWidgetsIndicationConfig.hashCode() : 0;
        result = 31 * result + (editableWidgetsIndicationConfig != null ? editableWidgetsIndicationConfig.hashCode() : 0);
        result = 31 * result + (widgetsIndicationConfig != null ? widgetsIndicationConfig.hashCode() : 0);
        return result;
    }
}
