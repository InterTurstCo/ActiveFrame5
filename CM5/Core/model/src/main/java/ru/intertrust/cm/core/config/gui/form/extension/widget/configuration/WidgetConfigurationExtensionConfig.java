package ru.intertrust.cm.core.config.gui.form.extension.widget.configuration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 21:36
 */
@Root(name = "widget-config-extension")
public class WidgetConfigurationExtensionConfig implements Dto {
    @ElementListUnion({
            @ElementList(entry="add-widgets", type=AddWidgetsConfig.class,required = false, inline = true),
            @ElementList(entry="delete-widgets", type=DeleteWidgetsConfig.class,required = false, inline = true),
            @ElementList(entry="replace-widgets", type=ReplaceWidgetsConfig.class,required = false, inline = true)
    })
    private List<FormExtensionOperation> widgetConfigurationExtensionOperations =  new ArrayList<>();

    public List<FormExtensionOperation> getWidgetConfigurationExtensionOperations() {
        return widgetConfigurationExtensionOperations;
    }

    public void setWidgetConfigurationExtensionOperations(List<FormExtensionOperation> widgetConfigurationExtensionOperations) {
        this.widgetConfigurationExtensionOperations = widgetConfigurationExtensionOperations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetConfigurationExtensionConfig that = (WidgetConfigurationExtensionConfig) o;

        if (widgetConfigurationExtensionOperations != null ? !widgetConfigurationExtensionOperations
                .equals(that.widgetConfigurationExtensionOperations) : that.widgetConfigurationExtensionOperations != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return widgetConfigurationExtensionOperations != null ? widgetConfigurationExtensionOperations.hashCode() : 0;
    }
}
