package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "date-format")
public class DateFormatConfig implements Dto {
    @Attribute(name = "pattern")
    private String pattern;

    @Attribute(name = "style")
    private String style;

    @Element(name = "field-paths")
    private FieldPathsConfig fieldsPathConfig;

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

    public FieldPathsConfig getFieldsPathConfig() {
        return fieldsPathConfig;
    }

    public void setFieldsPathConfig(FieldPathsConfig fieldsPathConfig) {
        this.fieldsPathConfig = fieldsPathConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DateFormatConfig that = (DateFormatConfig) o;

        if (fieldsPathConfig != null ? !fieldsPathConfig.equals(that.fieldsPathConfig) : that.fieldsPathConfig != null) {
            return false;
        }
        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) {
            return false;
        }
        if (style != null ? !style.equals(that.style) : that.style != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + (style != null ? style.hashCode() : 0);
        result = 31 * result + (fieldsPathConfig != null ? fieldsPathConfig.hashCode() : 0);
        return result;
    }
}
