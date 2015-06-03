package ru.intertrust.cm.core.config.gui.form.extension.widget.groups;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.2015
 *         Time: 0:20
 */
@Root(name = "widget-groups-extension")
public class WidgetGroupsExtensionConfig implements Dto {
    @ElementListUnion({
            @ElementList(entry="add-widget-groups", type=AddWidgetGroupsConfig.class,required = false, inline = true),
            @ElementList(entry="delete-widget-groups", type=DeleteWidgetGroupsConfig.class,required = false, inline = true),
            @ElementList(entry="replace-widget-groups", type=ReplaceWidgetGroupsConfig.class,required = false, inline = true)
    })
    private List<FormExtensionOperation> widgetGroupsExtensionOperations = new ArrayList<>();

    public List<FormExtensionOperation> getWidgetGroupsExtensionOperations() {
        return widgetGroupsExtensionOperations;
    }

    public void setWidgetGroupsExtensionOperations(List<FormExtensionOperation> widgetGroupsExtensionOperations) {
        this.widgetGroupsExtensionOperations = widgetGroupsExtensionOperations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetGroupsExtensionConfig that = (WidgetGroupsExtensionConfig) o;

        if (widgetGroupsExtensionOperations != null ? !widgetGroupsExtensionOperations.equals(that
                .widgetGroupsExtensionOperations) : that.widgetGroupsExtensionOperations != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return widgetGroupsExtensionOperations != null ? widgetGroupsExtensionOperations.hashCode() : 0;
    }
}
