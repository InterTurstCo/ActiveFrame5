package ru.intertrust.cm.core.gui.impl.server.filters;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFilterConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.gui.api.server.filters.SelectionFiltersBuilder;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 14:01
 */

public class SelectionFiltersBuilderImpl extends ComplicatedFiltersBuilder<SelectionFiltersConfig>
        implements SelectionFiltersBuilder{

    @Override
    boolean prepareComplicatedFilters(SelectionFiltersConfig config, ComplicatedFiltersParams params, List<Filter> filters) {
        boolean filtersWerePrepared = false;
        if (config != null && WidgetUtil.isNotEmpty(config.getFilterConfigs())) {
            List<SelectionFilterConfig> filtersConfigs = config.getFilterConfigs();
            for (SelectionFilterConfig filterConfig : filtersConfigs) {
                Filter filter = prepareComplicatedFilter(filterConfig, params);
                if (filter != null) {
                    filters.add(filter);
                    filtersWerePrepared = true;
                }
            }
        }
        return filtersWerePrepared;
    }

    @Override
    public boolean prepareSelectionFilters(SelectionFiltersConfig config, ComplicatedFiltersParams params, List<Filter> filters) {
        return prepareComplicatedFilters(config, params, filters);
    }
}
