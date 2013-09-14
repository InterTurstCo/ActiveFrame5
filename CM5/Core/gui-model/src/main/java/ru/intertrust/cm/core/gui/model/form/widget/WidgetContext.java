package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:54
 */
public class WidgetContext implements Dto {
    private Id rootObjectId;
    private WidgetConfig widgetConfig;

    public Id getRootObjectId() {
        return rootObjectId;
    }

    public void setRootObjectId(Id rootObjectId) {
        this.rootObjectId = rootObjectId;
    }

    public WidgetConfig getWidgetConfig() {
        return widgetConfig;
    }

    public void setWidgetConfig(WidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }
}
