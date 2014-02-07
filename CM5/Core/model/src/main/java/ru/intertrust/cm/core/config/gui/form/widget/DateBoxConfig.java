package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 14:17
 */
@Root(name = "date-box")
public class DateBoxConfig extends WidgetConfig implements Dto {

    @Element(name ="range-start", required = false)
    private RangeStartConfig rangeStartConfig;

    @Element(name ="range-end", required = false)
    private RangeEndConfig rangeEndConfig;

    public RangeStartConfig getRangeStartConfig() {
        return rangeStartConfig;
    }

    public void setRangeStartConfig(RangeStartConfig rangeStartConfig) {
        this.rangeStartConfig = rangeStartConfig;
    }

    public RangeEndConfig getRangeEndConfig() {
        return rangeEndConfig;
    }

    public void setRangeEndConfig(RangeEndConfig rangeEndConfig) {
        this.rangeEndConfig = rangeEndConfig;
    }

    @Override
    public String getComponentName() {
        return "date-box";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DateBoxConfig config = (DateBoxConfig) o;

        if (rangeEndConfig != null ? !rangeEndConfig.equals(config.rangeEndConfig) : config.rangeEndConfig != null)
            return false;
        if (rangeStartConfig != null ? !rangeStartConfig.equals(config.rangeStartConfig) : config.rangeStartConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (rangeStartConfig != null ? rangeStartConfig.hashCode() : 0);
        result = 31 * result + (rangeEndConfig != null ? rangeEndConfig.hashCode() : 0);
        return result;
    }
}
