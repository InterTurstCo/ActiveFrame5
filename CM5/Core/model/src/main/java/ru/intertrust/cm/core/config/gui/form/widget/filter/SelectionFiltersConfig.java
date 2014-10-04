package ru.intertrust.cm.core.config.gui.form.widget.filter;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
@Root(name = "selection-filters")
public class SelectionFiltersConfig extends AbstractFiltersConfig implements Dto {
    @Attribute(name = "row-limit", required = false)
    private int rowLimit = -1;

    @Attribute(name = "max-tooltip-width", required = false)
    private String maxTooltipWidth;

    public String getMaxTooltipHeight() {
        return maxTooltipHeight;
    }

    public void setMaxTooltipHeight(String maxTooltipHeight) {
        this.maxTooltipHeight = maxTooltipHeight;
    }

    public String getMaxTooltipWidth() {
        return maxTooltipWidth;
    }

    public void setMaxTooltipWidth(String maxTooltipWidth) {
        this.maxTooltipWidth = maxTooltipWidth;
    }

    public int getRowLimit() {
        return rowLimit < 0 ? -1 : rowLimit;
    }

    public void setRowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
    }

    @Attribute(name = "max-tooltip-height", required = false)
    private String maxTooltipHeight;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SelectionFiltersConfig that = (SelectionFiltersConfig) o;

        if (rowLimit != that.rowLimit) {
            return false;
        }
        if (maxTooltipHeight != null ? !maxTooltipHeight.equals(that.maxTooltipHeight) : that.maxTooltipHeight != null){
            return false;
        }
        if (maxTooltipWidth != null ? !maxTooltipWidth.equals(that.maxTooltipWidth) : that.maxTooltipWidth != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rowLimit;
        result = 31 * result + (maxTooltipWidth != null ? maxTooltipWidth.hashCode() : 0);
        result = 31 * result + (maxTooltipHeight != null ? maxTooltipHeight.hashCode() : 0);
        return result;
    }
}
