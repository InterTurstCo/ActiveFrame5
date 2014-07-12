package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.07.2014
 *         Time: 22:38
 */
public class LinkedTableTooltipResponse implements Dto {
    private List<RowItem> rowItems;

    public LinkedTableTooltipResponse() {
    }

    public LinkedTableTooltipResponse(List<RowItem> rowItems) {
        this.rowItems = rowItems;
    }

    public List<RowItem> getRowItems() {
        return rowItems;
    }


}
