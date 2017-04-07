package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 04.04.2016
 * Time: 10:04
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "ignore-form-read-only-state")
public class IgnoreFormReadOnlyStateConfig implements Dto {
    @Attribute(name = "value", required = true)
    private boolean value = false;

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IgnoreFormReadOnlyStateConfig that = (IgnoreFormReadOnlyStateConfig) o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }
}

