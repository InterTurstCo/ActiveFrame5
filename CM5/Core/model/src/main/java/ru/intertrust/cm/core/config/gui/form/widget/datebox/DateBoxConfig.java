package ru.intertrust.cm.core.config.gui.form.widget.datebox;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 14:17
 */
@Root(name = "date-box")
public class DateBoxConfig extends WidgetConfig implements Dto {

    @Attribute(name = "unmanaged-type", required = false)
    private String unmanagedType;

    @Element(name = "time-zone-id", required = false)
    private String timeZoneId;

    @Element(name = "pattern", required = false)
    private String pattern;

    @Element(name = "display-time-zone-choice", required = false)
    private boolean displayTimeZoneChoice;

    @Element(name ="range-start", required = false)
    private RangeStartConfig rangeStartConfig;

    @Element(name ="range-end", required = false)
    private RangeEndConfig rangeEndConfig;

    @Element(name ="display-time-box", required = false)
    private boolean displayTimeBox;

    public String getTimeZoneId() {
        return timeZoneId == null ? ModelUtil.DEFAULT_TIME_ZONE_ID : timeZoneId;
    }

    public String getPattern() {
        return pattern == null ? ModelUtil.DEFAULT_DATE_PATTERN : pattern;
    }

    public boolean isDisplayTimeZoneChoice() {
        return displayTimeZoneChoice;
    }

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

    public boolean isDisplayTimeBox() {
        return displayTimeBox;
    }

    public void setDisplayTimeBox(boolean displayTimeBox) {
        this.displayTimeBox = displayTimeBox;
    }

    public String getUnmanagedType() {
        return unmanagedType;
    }

    @Override
    public String getComponentName() {
        return "date-box";
    }

    @Override
    public String getLogicalValidatorComponentName() {
        return "dateBoxLogicalValidator";
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        DateBoxConfig other = (DateBoxConfig) obj;
        if (timeZoneId == null ? other.timeZoneId != null : !timeZoneId.equals(other.timeZoneId)) {
            return false;
        }
        if (pattern == null ? other.pattern != null : !pattern.equals(other.pattern)) {
            return false;
        }
        if (displayTimeZoneChoice != other.displayTimeZoneChoice) {
            return false;
        }
        if (rangeEndConfig != null ? !rangeEndConfig.equals(other.rangeEndConfig) : other.rangeEndConfig != null) {
            return false;
        }
        if (rangeStartConfig != null
                ? !rangeStartConfig.equals(other.rangeStartConfig) : other.rangeStartConfig != null) {
            return false;
        }
        if (displayTimeBox != other.displayTimeBox) {
            return false;
        }
        if (unmanagedType != null ? !unmanagedType.equals(other.unmanagedType) : other.unmanagedType != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (timeZoneId == null ? 0 : timeZoneId.hashCode());
        result = 31 * result + (pattern == null ? 0 : pattern.hashCode());
        result = 31 * result + (displayTimeZoneChoice ? 0 : 1);
        result = 31 * result + (rangeStartConfig != null ? rangeStartConfig.hashCode() : 0);
        result = 31 * result + (rangeEndConfig != null ? rangeEndConfig.hashCode() : 0);
        result = 31 * result + (unmanagedType != null ? unmanagedType.hashCode() : 0);
        return result;
    }
}
