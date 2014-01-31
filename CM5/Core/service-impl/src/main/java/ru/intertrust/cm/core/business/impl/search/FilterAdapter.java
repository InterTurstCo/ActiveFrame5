package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;

public interface FilterAdapter<F extends SearchFilter> {

    String getFilterValue(F filter);

    String getFieldTypeSuffix(F filter);
}
