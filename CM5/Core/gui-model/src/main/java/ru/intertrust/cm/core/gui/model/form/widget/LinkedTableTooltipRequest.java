package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.07.2014
 *         Time: 22:35
 */
public class LinkedTableTooltipRequest implements Dto {
    private LinkedDomainObjectsTableConfig config;
    private List<Id> selectedIds;
    private ComplicatedFiltersParams filtersParams;

    public LinkedTableTooltipRequest() {
    }

    public LinkedTableTooltipRequest(LinkedDomainObjectsTableConfig config, List<Id> selectedIds, ComplicatedFiltersParams filtersParams) {
        this.config = config;
        this.selectedIds = selectedIds;
        this.filtersParams = filtersParams;
    }

    public LinkedDomainObjectsTableConfig getConfig() {
        return config;
    }

    public List<Id> getSelectedIds() {
        return selectedIds;
    }

    public ComplicatedFiltersParams getFiltersParams() {
        return filtersParams;
    }
}
