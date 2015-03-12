package ru.intertrust.cm.core.gui.impl.server.filters;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.gui.form.widget.filter.ComplexParamConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.NullFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.ExtraFilterConfig;
import ru.intertrust.cm.core.gui.api.server.filters.CollectionExtraFiltersBuilder;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 14:01
 */

public class CollectionExtraFiltersBuilderImpl extends ComplexFiltersBuilder<CollectionExtraFiltersConfig>
        implements CollectionExtraFiltersBuilder{

    @Override
    public boolean prepareCollectionExtraFilters(CollectionExtraFiltersConfig config, ComplexFiltersParams params,
                                                 List<Filter> filters) {
        return prepareComplexFilters(config, params, filters);
    }

    @Override
    boolean prepareComplexFilters(CollectionExtraFiltersConfig config, ComplexFiltersParams params,
                                  List<Filter> filters) {
        boolean filtersWerePrepared = false;
        if (config != null && WidgetUtil.isNotEmpty(config.getFilterConfigs())) {
            List<ExtraFilterConfig> filtersConfigs = config.getFilterConfigs();
            for (NullFilterConfig<? extends ComplexParamConfig> filterConfig : filtersConfigs) {
                Filter filter = prepareComplexFilter(filterConfig, params);
                if (filter != null) {
                    filters.add(filter);
                    filtersWerePrepared = true;
                }
            }
        }

        return prepareInputTextFilter(params, filters) || filtersWerePrepared;
    }

    private boolean prepareInputTextFilter(ComplexFiltersParams params, List<Filter> filters) {
        boolean result = false;
        if (params != null) {
            String text = params.getInputFilterValue();
            String filterName = params.getInputFilterName();
            if (text != null && !text.isEmpty() && !text.equals("*")) {
                Filter textFilter = new Filter();
                textFilter.setFilter(filterName);
                textFilter.addCriterion(0, new StringValue(text + "%"));
                filters.add(textFilter);
                result = true;
            }
        }
        return result;
    }
}
