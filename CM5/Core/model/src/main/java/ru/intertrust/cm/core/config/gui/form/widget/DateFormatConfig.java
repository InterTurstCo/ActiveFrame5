package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "date-format")
public class DateFormatConfig extends AbstractTypeFormatConfig {
    @Attribute(name = "pattern")
    private String pattern;

    @Attribute(name = "style")
    private String style;

    @Element(name = "time-zone", required = false)
    private TimeZoneConfig timeZoneConfig;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public TimeZoneConfig getTimeZoneConfig() {
        return timeZoneConfig;
    }

    public void setTimeZoneConfig(TimeZoneConfig timeZoneConfig) {
        this.timeZoneConfig = timeZoneConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateFormatConfig)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DateFormatConfig that = (DateFormatConfig) o;

        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) {
            return false;
        }
        if (style != null ? !style.equals(that.style) : that.style != null) {
            return false;
        }
        if (timeZoneConfig != null ? !timeZoneConfig.equals(that.timeZoneConfig) : that.timeZoneConfig != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        result = 31 * result + (style != null ? style.hashCode() : 0);
        result = 31 * result + (timeZoneConfig != null ? timeZoneConfig.hashCode() : 0);
        return result;
    }
}
