package ru.intertrust.cm.core.gui.model.filters;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 13:42
 */
public class InitialFiltersParams {
    private Id rootId;
    private List<String> excludedInitialFilterNames;
    private Map<String,CollectionColumnProperties> filterNameColumnPropertiesMap;

    public InitialFiltersParams() {
    }

    public InitialFiltersParams(Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap) {
        this.filterNameColumnPropertiesMap = filterNameColumnPropertiesMap;
    }

    public InitialFiltersParams(Id rootId, Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap) {
        this.rootId = rootId;
        this.filterNameColumnPropertiesMap = filterNameColumnPropertiesMap;
    }

    public InitialFiltersParams(List<String> excludedInitialFilterNames,
                                Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap) {
        this(filterNameColumnPropertiesMap);
        this.excludedInitialFilterNames = excludedInitialFilterNames;
    }

    public InitialFiltersParams(Id rootId, List<String> excludedInitialFilterNames,
                                Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap) {
        this(excludedInitialFilterNames, filterNameColumnPropertiesMap);
        this.rootId = rootId;
    }

    public Id getRootId() {
        return rootId;
    }

    public void setRootId(Id rootId) {
        this.rootId = rootId;
    }

    public List<String> getExcludedInitialFilterNames() {
        return excludedInitialFilterNames;
    }

    public void setExcludedInitialFilterNames(List<String> excludedInitialFilterNames) {
        this.excludedInitialFilterNames = excludedInitialFilterNames;
    }

    public Map<String, CollectionColumnProperties> getFilterNameColumnPropertiesMap() {
        return filterNameColumnPropertiesMap;
    }

    public void setFilterNameColumnPropertiesMap(Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap) {
        this.filterNameColumnPropertiesMap = filterNameColumnPropertiesMap;
    }
}
