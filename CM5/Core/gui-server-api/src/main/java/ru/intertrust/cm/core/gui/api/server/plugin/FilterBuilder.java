package ru.intertrust.cm.core.gui.api.server.plugin;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.07.2014
 *         Time: 9:50
 */

public interface FilterBuilder extends ComponentHandler {
    public boolean prepareInitialFilters(InitialFiltersConfig initialFiltersConfig,
                                         List<String> excludedInitialFilterNames, List<Filter> filters);

    public boolean prepareSelectionFilters(SelectionFiltersConfig selectionFiltersConfig,
                                           List<String> excludedInitialFilterNames, List<Filter> filters);

}
