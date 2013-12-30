package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;

public class TextFilterAdapter implements FilterAdapter {

    @Override
    public String getFilterValue(SearchFilter filter) {
        TextSearchFilter textFilter = (TextSearchFilter) filter;
        return textFilter.getText();
    }

}
