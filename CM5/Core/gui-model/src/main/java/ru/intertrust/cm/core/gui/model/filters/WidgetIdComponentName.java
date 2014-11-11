package ru.intertrust.cm.core.gui.model.filters;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.11.2014
 *         Time: 8:48
 */
public class WidgetIdComponentName implements Dto {
    private String widgetId;
    private String componentName;

    public WidgetIdComponentName() {
    }

    public WidgetIdComponentName(String widgetId, String componentName) {
        this.widgetId = widgetId;
        this.componentName = componentName;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
