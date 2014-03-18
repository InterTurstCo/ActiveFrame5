package ru.intertrust.cm.core.business.impl.search;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.CombiningFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

public class CombiningFilterAdapter implements FilterAdapter<CombiningFilter> {

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Override
    @SuppressWarnings("unchecked")
    public String getFilterString(CombiningFilter filter, SearchQuery query) {
        if (filter.getFilters() == null || filter.getFilters().size() == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean multiple = false;
        for (SearchFilter nested : filter.getFilters()) {
            @SuppressWarnings("rawtypes")
            FilterAdapter adapter = searchFilterImplementorFactory.createImplementorFor(nested.getClass());
            String value = adapter.getFilterString(nested, query);
            if (value == null || value.length() == 0) {
                continue;
            }
            if (result.length() > 0) {
                result.append(CombiningFilter.OR == filter.getOperation() ? " OR " : " AND ");
                multiple = true;
            }
            result.append(value);
        }
        if (multiple) {
            result.insert(0, "(").append(")");
        }
        return result.toString();
    }
}
