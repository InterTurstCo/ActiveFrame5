package ru.intertrust.cm.core.gui.api.server.filters;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.gui.navigation.InitialFiltersConfig;
import ru.intertrust.cm.core.gui.model.filters.InitialFiltersParams;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.11.2014
 *         Time: 13:23
 */
public interface InitialFiltersBuilder {
    boolean prepareInitialFilters(InitialFiltersConfig initialFiltersConfig, InitialFiltersParams params,List<Filter> filters);

}
