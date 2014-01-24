package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.01.14
 *         Time: 11:40
 */
public class AllValuesEmptyMessageConfig implements Dto {
    @Attribute(name = "value", required = false)
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AllValuesEmptyMessageConfig that = (AllValuesEmptyMessageConfig) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
