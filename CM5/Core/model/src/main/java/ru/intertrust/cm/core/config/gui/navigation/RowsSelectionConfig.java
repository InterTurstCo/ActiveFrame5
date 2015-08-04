package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.08.2015
 *         Time: 19:16
 */
@Root(name = "rows-selection")
public class RowsSelectionConfig implements Dto {
    @Attribute(name = "default-state", required = false)
    private String defaultState;

    public RowsSelectionDefaultState getDefaultState() {
        return RowsSelectionDefaultState.forCode(defaultState);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RowsSelectionConfig that = (RowsSelectionConfig) o;

        if (defaultState != null ? !defaultState.equals(that.defaultState) : that.defaultState != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return defaultState != null ? defaultState.hashCode() : 0;
    }
}
