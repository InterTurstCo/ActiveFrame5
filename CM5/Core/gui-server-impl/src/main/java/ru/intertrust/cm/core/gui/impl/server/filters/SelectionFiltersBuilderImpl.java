package ru.intertrust.cm.core.gui.impl.server.filters;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.gui.api.server.filters.SelectionFiltersBuilder;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 14:01
 */

public class SelectionFiltersBuilderImpl extends ComplexFiltersBuilder<SelectionFiltersConfig>
        implements SelectionFiltersBuilder{

    @Override
    boolean prepareComplexFilters(SelectionFiltersConfig config, ComplexFiltersParams params, List<Filter> filters) {
        boolean filtersWerePrepared = false;
        if (config != null && WidgetUtil.isNotEmpty(config.getFilterConfigs())) {
            List<SelectionFilterConfig> filtersConfigs = config.getFilterConfigs();
            for (SelectionFilterConfig filterConfig : filtersConfigs) {
                Filter filter = prepareComplexFilter(filterConfig, params);
                if (filter != null) {
                    filters.add(filter);
                    filtersWerePrepared = true;
                }
            }
        }
        return filtersWerePrepared;
    }

    @Override
    public boolean prepareSelectionFilters(SelectionFiltersConfig config, ComplexFiltersParams params, List<Filter> filters) {
        return prepareComplexFilters(config, params, filters);
    }
}
