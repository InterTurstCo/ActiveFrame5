package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.12.13
 *         Time: 13:15
 */
public class WidgetItemsResponse implements Dto {
    private LinkedHashMap<Id, String> listValues;
    private boolean tooltipContent;

    public LinkedHashMap<Id, String> getListValues() {
        return listValues;
    }

    public void setListValues(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    public boolean isTooltipContent() {
        return tooltipContent;
    }

    public void setTooltipContent(boolean tooltipContent) {
        this.tooltipContent = tooltipContent;
    }
}
