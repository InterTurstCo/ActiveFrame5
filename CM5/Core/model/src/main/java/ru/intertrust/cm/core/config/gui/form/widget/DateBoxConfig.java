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

    @Element(name = "time-zone-id", required = false)
    private String timeZoneId;

    @Element(name = "pattern", required = false)
    private String pattern;

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

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public String getComponentName() {
        return "date-box";
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        DateBoxConfig other = (DateBoxConfig) obj;

        if (rangeEndConfig != null ? !rangeEndConfig.equals(other.rangeEndConfig) : other.rangeEndConfig != null) {
            return false;
        }
        if (rangeStartConfig != null
                ? !rangeStartConfig.equals(other.rangeStartConfig) : other.rangeStartConfig != null) {
            return false;
        }
        if (timeZoneId == null ? other.timeZoneId != null : !timeZoneId.equals(other.timeZoneId)) {
            return false;
        }
        if (pattern == null ? other.pattern != null : !pattern.equals(other.pattern)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (rangeStartConfig != null ? rangeStartConfig.hashCode() : 0);
        result = 31 * result + (rangeEndConfig != null ? rangeEndConfig.hashCode() : 0);
        result = 31 * result + (timeZoneId == null ? 0 : timeZoneId.hashCode());
        result = 31 * result + (pattern == null ? 0 : pattern.hashCode());
        return result;
    }
}
