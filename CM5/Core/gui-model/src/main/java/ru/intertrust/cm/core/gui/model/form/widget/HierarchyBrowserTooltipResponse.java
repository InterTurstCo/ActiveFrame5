package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.07.2014
 *         Time: 22:37
 */
public class HierarchyBrowserTooltipResponse implements Dto {
    private ArrayList<HierarchyBrowserItem> items;
    private ArrayList<Id> selectedIds;
    public HierarchyBrowserTooltipResponse() {
    }

    public HierarchyBrowserTooltipResponse(ArrayList<HierarchyBrowserItem> items, ArrayList<Id> selectedIds) {
        this.items = items;
        this.selectedIds = selectedIds;
    }

    public ArrayList<Id> getSelectedIds() {
        return selectedIds;
    }

    public ArrayList<HierarchyBrowserItem> getItems() {
        return items;
    }

}
