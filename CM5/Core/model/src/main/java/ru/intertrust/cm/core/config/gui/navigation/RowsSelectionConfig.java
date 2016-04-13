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

    @Attribute(name = "multi-selection", required = false)
    private Boolean multiSelection;

    @Attribute(name = "on-selection-change-component", required = false)
    private String component;

    public RowsSelectionDefaultState getDefaultState() {
        return RowsSelectionDefaultState.forCode(defaultState);
    }

    public boolean isMultiSelection() {
        return multiSelection;
    }

    public void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
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
        if (multiSelection != null ? !multiSelection.equals(that.multiSelection) : that.multiSelection != null) {
            return false;
        }
        if (component != null ? !component.equals(that.component) : that.component != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result =  defaultState != null ? defaultState.hashCode() : 0;
        result = 31 * result + (multiSelection != null ? multiSelection.hashCode() : 0);
        result = 31 * result + (component != null ? component.hashCode() : 0);
        return result;
    }
}
