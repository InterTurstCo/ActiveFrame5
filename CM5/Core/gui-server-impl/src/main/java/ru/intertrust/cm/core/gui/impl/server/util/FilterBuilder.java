package ru.intertrust.cm.core.gui.impl.server.util;

import ru.intertrust.cm.core.business.api.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.01.14
 *         Time: 13:15
 */
public class FilterBuilder {
    private static final String EXCLUDED_IDS_FILTER = "idsExcluded";
    private static final String INCLUDED_IDS_FILTER = "idsIncluded";

    public static Filter prepareFilter(Set<Id> excludeIds, String type) {
        List<ReferenceValue> idsCriterion = new ArrayList<>();
        for (Id id : excludeIds) {
            idsCriterion.add(new ReferenceValue(id));
        }
        Filter filter = createRequiredFilter(idsCriterion, type);
        return filter;
    }

    private static Filter createRequiredFilter(List<ReferenceValue> idsCriterion, String type){
        if (EXCLUDED_IDS_FILTER.equalsIgnoreCase(type)){
            Filter filter = new IdsExcludedFilter(idsCriterion);
            filter.setFilter(EXCLUDED_IDS_FILTER  + (int)(65 * Math.random()));
            return filter;
        }
        if (INCLUDED_IDS_FILTER.equalsIgnoreCase(type)) {
            Filter filter = new IdsIncludedFilter(idsCriterion);
            filter.setFilter(INCLUDED_IDS_FILTER + (int)(65 * Math.random()));
            return filter;
        }
        return null;
    }
}
