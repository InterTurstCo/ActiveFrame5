package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFiltersConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.CollectionColumnProperties;

import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.07.2014
 *         Time: 9:50
 */

public interface FilterBuilder extends ComponentHandler {
    boolean prepareInitialFilters(AbstractFiltersConfig  abstractFiltersConfig,
                                  List<String> excludedInitialFilterNames,  List<Filter> filters, Map<String,
                                          CollectionColumnProperties> filterNameColumnPropertiesMap);

    boolean prepareSelectionFilters(AbstractFiltersConfig  selectionFiltersConfig,
                                           List<String> excludedInitialFilterNames, List<Filter> filters);

}
