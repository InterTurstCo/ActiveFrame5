package ru.intertrust.cm.core.config.gui.form.widget.datebox;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * User: IPetrov
 * Date: 06.02.14
 * Time: 13:07
 * окончание интервала дат
 */
@Root(name ="range-end")
public class RangeEndConfig implements Dto {
    @Attribute(name = "widget-id")
    private String widgetId;

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RangeEndConfig that = (RangeEndConfig) o;

        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return widgetId != null ? widgetId.hashCode() : 0;
    }
}
