package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;

public interface FilterAdapter {

    String getFilterValue(SearchFilter filter);
}
