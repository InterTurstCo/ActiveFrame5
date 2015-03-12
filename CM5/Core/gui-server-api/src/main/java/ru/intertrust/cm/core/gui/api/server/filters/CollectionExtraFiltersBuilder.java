package ru.intertrust.cm.core.gui.api.server.filters;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 13:24
 */
public interface CollectionExtraFiltersBuilder {
    boolean prepareCollectionExtraFilters(CollectionExtraFiltersConfig config, ComplexFiltersParams params, List<Filter> filters);
}
