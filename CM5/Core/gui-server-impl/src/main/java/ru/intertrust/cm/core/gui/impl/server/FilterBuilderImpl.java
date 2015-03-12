package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.api.server.filters.CollectionExtraFiltersBuilder;
import ru.intertrust.cm.core.gui.api.server.filters.InitialFiltersBuilder;
import ru.intertrust.cm.core.gui.api.server.filters.SelectionFiltersBuilder;
import ru.intertrust.cm.core.gui.api.server.plugin.FilterBuilder;
import ru.intertrust.cm.core.gui.impl.server.util.FilterBuilderUtil;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.InitialFiltersParams;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.07.2014
 *         Time: 9:51
 */

public class FilterBuilderImpl implements FilterBuilder {
    @Autowired
    private InitialFiltersBuilder initialFiltersBuilder;

    @Autowired
    private SelectionFiltersBuilder selectionFiltersBuilder;

    @Autowired
    private CollectionExtraFiltersBuilder collectionExtraFiltersBuilder;


    public boolean prepareInitialFilters(InitialFiltersConfig config, InitialFiltersParams params, List<Filter> filters) {
        return initialFiltersBuilder.prepareInitialFilters(config, params, filters);
    }

    @Override
    public boolean prepareSelectionFilters(SelectionFiltersConfig config, ComplexFiltersParams params, List<Filter> filters) {
        return selectionFiltersBuilder.prepareSelectionFilters(config, params, filters);
    }

    @Override
    public boolean prepareExtraFilters(CollectionExtraFiltersConfig config, ComplexFiltersParams params, List<Filter> filters) {
        return collectionExtraFiltersBuilder.prepareCollectionExtraFilters(config, params, filters);
    }


    public boolean prepareInitialFilters(AbstractFiltersConfig abstractFiltersConfig, List<String> excludedInitialFilterNames,
                                         List<Filter> filters, Map<String, CollectionColumnProperties> filterNameColumnPropertiesMap) {
        return false; //not supported for now
    }

    public boolean prepareSelectionFilters(AbstractFiltersConfig selectionFiltersConfig,
                                           List<String> excludedInitialFilterNames, List<Filter> filters) {

        return false; //not supported for now
    }

    public void prepareIncludedIdsFilter(Collection<Id> ids, List<Filter> filters){
        filters.add(FilterBuilderUtil.prepareFilter(ids, FilterBuilderUtil.INCLUDED_IDS_FILTER));
    }

    @Override
    public void prepareExcludedIdsFilter(Collection<Id> ids, List<Filter> filters) {
        filters.add(FilterBuilderUtil.prepareFilter(ids, FilterBuilderUtil.EXCLUDED_IDS_FILTER));
    }

}
