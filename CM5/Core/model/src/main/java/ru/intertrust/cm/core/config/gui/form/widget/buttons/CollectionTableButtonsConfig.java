package ru.intertrust.cm.core.config.gui.form.widget.buttons;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.04.2015
 *         Time: 9:12
 */
@Root(name = "collection-table-buttons")
public class CollectionTableButtonsConfig implements Dto {
    @Attribute(name = "display-all-possible", required = true)
    private boolean displayAllPossible;

    public boolean isDisplayAllPossible() {
        return displayAllPossible;
    }

    public void setDisplayAllPossible(boolean displayAllPossible) {
        this.displayAllPossible = displayAllPossible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionTableButtonsConfig that = (CollectionTableButtonsConfig) o;

        if (displayAllPossible != that.displayAllPossible) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (displayAllPossible ? 1 : 0);
    }
}
