package ru.intertrust.cm.core.business.impl.search.simple;

import ru.intertrust.cm.core.business.api.simpledata.SimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;

public interface SimpleDataSearchFilterQueryFactory {
    String getQuery(SimpleDataConfig config, SimpleDataSearchFilter filter);
}
