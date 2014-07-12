package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.07.2014
 *         Time: 22:31
 */
public class HierarchyBrowserTooltipRequest implements Dto {
    private HierarchyBrowserConfig hierarchyBrowserConfig;
    private ArrayList<Id> selectedIds;
    public HierarchyBrowserTooltipRequest() {
    }

    public HierarchyBrowserTooltipRequest(HierarchyBrowserConfig hierarchyBrowserConfig, ArrayList<Id> selectedIds) {
        this.hierarchyBrowserConfig = hierarchyBrowserConfig;
        this.selectedIds = selectedIds;
    }

    public HierarchyBrowserConfig getHierarchyBrowserConfig() {
        return hierarchyBrowserConfig;
    }

    public ArrayList<Id> getSelectedIds() {
        return selectedIds;
    }
}
