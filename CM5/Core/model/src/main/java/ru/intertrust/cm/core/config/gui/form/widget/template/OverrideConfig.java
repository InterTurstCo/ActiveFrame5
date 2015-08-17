package ru.intertrust.cm.core.config.gui.form.widget.template;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.converter.OverrideConfigConverter;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.08.2015
 *         Time: 10:30
 */
@Root(name = "override")
@Convert(OverrideConfigConverter.class)
public class OverrideConfig implements Dto {
    @Element(type=WidgetConfig.class)
    private WidgetConfig widgetConfig;

    public WidgetConfig getWidgetConfig() {
        return widgetConfig;
    }

    public void setWidgetConfig(WidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OverrideConfig that = (OverrideConfig) o;

        if (widgetConfig != null ? !widgetConfig.equals(that.widgetConfig) : that.widgetConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return widgetConfig != null ? widgetConfig.hashCode() : 0;
    }
}
