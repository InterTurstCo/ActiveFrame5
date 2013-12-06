package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 02/12/13
 *         Time: 12:05 PM
 */
@Root(name = "display-chosen-values")
public class DisplayChosenValuesConfig implements Dto {
    @Attribute(name = "value", required = false)
    private boolean displayChosenValues = false;

    public boolean isDisplayChosenValues() {
        return displayChosenValues;
    }

    public void setDisplayChosenValues(boolean displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DisplayChosenValuesConfig that = (DisplayChosenValuesConfig) o;

        if (displayChosenValues != that.displayChosenValues) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (displayChosenValues ? 1 : 0);
    }
}
