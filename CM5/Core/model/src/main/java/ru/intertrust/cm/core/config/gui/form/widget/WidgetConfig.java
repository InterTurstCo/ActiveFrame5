package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;
/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 14:16
 */
public abstract class WidgetConfig implements Dto {
    @Attribute(name = "id", required = false)
    protected String id;
    @Attribute(name = "read-only", required = false)
    protected boolean readOnly;
    @Element(name = "field-path", required = false)
    protected FieldPathConfig fieldPathConfig;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FieldPathConfig getFieldPathConfig() {
        return fieldPathConfig;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetConfig that = (WidgetConfig) o;

        if (readOnly != that.readOnly) {
            return false;
        }
        if (fieldPathConfig != null ? !fieldPathConfig.equals(that.fieldPathConfig) : that.fieldPathConfig != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (readOnly ? 1 : 0);
        result = 31 * result + (fieldPathConfig != null ? fieldPathConfig.hashCode() : 0);
        return result;
    }

    public abstract String getComponentName();

    public boolean handlesMultipleObjects() {
        return false;
    }
}
