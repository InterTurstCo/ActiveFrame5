package ru.intertrust.cm.core.gui.model.form.widget;

import java.util.ArrayList;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;


/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 22.10.13
 * Time: 16:22
 * To change this template use File | Settings | File Templates.
 */
public class SuggestBoxState extends TooltipWidgetState<SuggestBoxConfig> {
    private Set<Id> selectedIds;
    private SuggestBoxConfig suggestBoxConfig;

    public void setSelectedIds(Set<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public Set<Id> getSelectedIds() {
        return selectedIds;
    }

    @Override
    public ArrayList<Id> getIds() {
        return selectedIds == null ? null : new ArrayList(selectedIds);
    }

    public SuggestBoxConfig getSuggestBoxConfig() {
        return suggestBoxConfig;
    }

    public void setSuggestBoxConfig(SuggestBoxConfig suggestBoxConfig) {
        this.suggestBoxConfig = suggestBoxConfig;
    }

    @Override
    public SuggestBoxConfig getWidgetConfig() {
        return suggestBoxConfig;
    }
}
