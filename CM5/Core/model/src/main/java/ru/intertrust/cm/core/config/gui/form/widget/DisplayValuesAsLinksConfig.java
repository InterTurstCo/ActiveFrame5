package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 03/01/14
 *         Time: 12:05 PM
 */
@Root(name = "display-values-as-links")
public class DisplayValuesAsLinksConfig implements Dto {
    @Attribute(name = "value")
    private boolean value;

    @Attribute(name = "modal-window", required = false)
    private boolean modalWindow = true;

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean isModalWindow() {
        return modalWindow;
    }

    public void setModalWindow(boolean modalWindow) {
        this.modalWindow = modalWindow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DisplayValuesAsLinksConfig that = (DisplayValuesAsLinksConfig) o;

        if (value != that.value) {
            return false;
        }
        if (modalWindow != that.modalWindow) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }
}
