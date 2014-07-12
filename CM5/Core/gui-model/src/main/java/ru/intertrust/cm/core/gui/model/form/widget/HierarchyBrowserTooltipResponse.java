package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.07.2014
 *         Time: 22:37
 */
public class HierarchyBrowserTooltipResponse implements Dto {
    private ArrayList<HierarchyBrowserItem> items;
    private ArrayList<Id> selectedIds;
    private SelectionFiltersConfig selectionFiltersConfig;
    public HierarchyBrowserTooltipResponse() {
    }

    public HierarchyBrowserTooltipResponse(ArrayList<HierarchyBrowserItem> items, ArrayList<Id> selectedIds,
                                           SelectionFiltersConfig selectionFiltersConfig) {
        this.items = items;
        this.selectedIds = selectedIds;
        this.selectionFiltersConfig = selectionFiltersConfig;
    }

    public ArrayList<Id> getSelectedIds() {
        return selectedIds;
    }

    public ArrayList<HierarchyBrowserItem> getItems() {
        return items;
    }

    public SelectionFiltersConfig getSelectionFiltersConfig() {
        return selectionFiltersConfig;
    }
}
