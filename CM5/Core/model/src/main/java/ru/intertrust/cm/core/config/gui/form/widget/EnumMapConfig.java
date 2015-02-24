package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;

/**
 * @author Lesia Puhova
 *         Date: 03.10.14
 *         Time: 18:57
 */
@Root(name="map")
public class EnumMapConfig implements Dto {

    @Attribute(name="display-text", required = false)
    @Localizable
    private String displayText;

    @Attribute(name="value", required = false)
    private String value;

    @Attribute(name="null-value", required = false)
    private boolean nullValue;

    public String getDisplayText() {
        return displayText;
    }

    public String getValue() {
        return value;
    }

    public boolean isNullValue() {
        return nullValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EnumMapConfig that = (EnumMapConfig) o;
        if (nullValue != that.nullValue) {
            return false;
        }
        if (displayText != null ? !displayText.equals(that.displayText) : that.displayText != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = displayText != null ? displayText.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (nullValue ? 1 : 0);
        return result;
    }
}
