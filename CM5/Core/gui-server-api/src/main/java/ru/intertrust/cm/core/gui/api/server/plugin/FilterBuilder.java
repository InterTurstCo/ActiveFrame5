package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.filters.InitialFiltersParams;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.07.2014
 *         Time: 9:50
 */

public interface FilterBuilder {
    /**
     *
     * @deprecated use
     * boolean prepareInitialFilters(InitialFiltersConfig initialFiltersConfig, InitialFiltersParams params,List<Filter> filters)
     */
    @Deprecated
    boolean prepareInitialFilters(AbstractFiltersConfig  abstractFiltersConfig,
                                  List<String> excludedInitialFilterNames,  List<Filter> filters, Map<String,
                                          CollectionColumnProperties> filterNameColumnPropertiesMap);

    /**
     *
     * @deprecated use
     * boolean prepareSelectionFilters(SelectionFiltersConfig config, ComplexFiltersParams params, List<Filter> filters)
     */
    @Deprecated
    boolean prepareSelectionFilters(AbstractFiltersConfig  selectionFiltersConfig,
                                    List<String> excludedInitialFilterNames, List<Filter> filters);

    boolean prepareInitialFilters(InitialFiltersConfig config, InitialFiltersParams params,List<Filter> filters);

    boolean prepareSelectionFilters(SelectionFiltersConfig config, ComplexFiltersParams params, List<Filter> filters);

    boolean prepareExtraFilters(CollectionExtraFiltersConfig config, ComplexFiltersParams params, List<Filter> filters);

    void prepareIncludedIdsFilter(Collection<Id> ids, List<Filter> filters);

    void prepareExcludedIdsFilter(Collection<Id> ids, List<Filter> filters);

}
