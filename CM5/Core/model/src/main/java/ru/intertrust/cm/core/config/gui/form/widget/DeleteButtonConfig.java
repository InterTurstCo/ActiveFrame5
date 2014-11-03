package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 23.10.14
 *         Time: 13:34
 */
@Root(name="delete-button")
public class DeleteButtonConfig implements Dto {

    @Attribute(name="display")
    private boolean display;

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeleteButtonConfig that = (DeleteButtonConfig) o;

        if (display != that.display) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (display ? 1 : 0);
    }
}
