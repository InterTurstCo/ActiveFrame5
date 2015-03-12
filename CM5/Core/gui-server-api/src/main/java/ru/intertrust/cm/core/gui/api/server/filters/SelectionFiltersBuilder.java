package ru.intertrust.cm.core.gui.api.server.filters;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 14:05
 */
public interface SelectionFiltersBuilder {
    boolean prepareSelectionFilters(SelectionFiltersConfig config, ComplexFiltersParams params, List<Filter> filters);
}
