package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchData;

/**
 * User: IPetrov
 * Date: 09.01.14
 * Time: 10:43
 * Обработчик формы расширенного поиска
 */
@ComponentName("extended.search.form.plugin")
public class ExtendedSearchFormPluginHandler extends PluginHandler {
    @Autowired
    SearchService searchService;

    // запрос на поиск
    public IdentifiableObjectCollection extendedSearch(ExtendedSearchData extendedSearchData) {

        return searchService.search(extendedSearchData.getSearchQuery(),
                         extendedSearchData.getSearchQuery().getTargetObjectType(), extendedSearchData.getMaxResults());
    }

}
