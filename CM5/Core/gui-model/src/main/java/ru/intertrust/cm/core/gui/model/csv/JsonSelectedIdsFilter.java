package ru.intertrust.cm.core.gui.model.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

import java.util.ArrayList;
import java.util.List;

public class JsonSelectedIdsFilter {
    private List<String> selectedIds = null;
    private List<ReferenceValue> filterIds = null;

    public List<String> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(List<String> selectedIds) {
        this.selectedIds = selectedIds;
        this.filterIds = null;
    }

    @JsonIgnore
    public List<ReferenceValue> getFilterIds() {
        if (filterIds == null && selectedIds != null) {
            filterIds = new ArrayList<>(selectedIds.size());
            for (String strId : selectedIds) {
                if (strId != null) {
                    filterIds.add(new ReferenceValue(new RdbmsId(strId)));
                }
            }
        }
        return filterIds;
    }

}
