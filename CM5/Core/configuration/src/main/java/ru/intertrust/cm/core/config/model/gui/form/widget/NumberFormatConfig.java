package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 18:27
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "number-format")
public class NumberFormatConfig implements Dto{
    @Attribute(name = "pattern")
    private String pattern;

    @Element(name = "field-paths")
    private FieldPathsConfig fieldsPathConfig;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
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

        NumberFormatConfig that = (NumberFormatConfig) o;

        if (fieldsPathConfig != null ? !fieldsPathConfig.equals(that.fieldsPathConfig) : that.fieldsPathConfig != null) {
            return false;
        }
        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + (fieldsPathConfig != null ? fieldsPathConfig.hashCode() : 0);
        return result;
    }
}
