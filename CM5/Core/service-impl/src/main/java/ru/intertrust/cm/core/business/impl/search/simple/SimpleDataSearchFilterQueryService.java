package ru.intertrust.cm.core.business.impl.search.simple;

import ru.intertrust.cm.core.business.api.simpledata.SimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;

public interface SimpleDataSearchFilterQueryService {
    Class<?> getType();

    String prepareQuery(SimpleDataConfig config, SimpleDataSearchFilter filter);
}
