package ru.intertrust.cm.core.business.impl.search;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.CombiningFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.impl.search.SearchServiceImpl;

public class CombiningFilterAdapter implements CompositeFilterAdapter<CombiningFilter> {

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Override
    @SuppressWarnings("unchecked")
    public String getFilterString(CombiningFilter filter, SearchQuery query) {
        if (filter.getFilters().size() > 1) {
            throw new IllegalArgumentException("This method must not be called for composite filters");
        }

        if (filter.getFilters().size() == 0) {
            return null;
        }
        SearchFilter nestedFilter = filter.getFilters().get(0);
        FilterAdapter<SearchFilter> adapter = (FilterAdapter<SearchFilter>)
                searchFilterImplementorFactory.createImplementorFor(nestedFilter.getClass());
        return adapter.getFilterString(nestedFilter, query);
    }

    @Override
    public boolean isCompositeFilter(CombiningFilter filter) {
        return filter.getFilters().size() > 1;
    }

    @Override
    public SearchServiceImpl.ComplexQuery processCompositeFilter(CombiningFilter filter,
            SearchServiceImpl.ComplexQuery queryProcessor, SearchQuery query) {
        SearchServiceImpl.ComplexQuery nestedQuery = queryProcessor.newNestedQuery();
        nestedQuery.combineOperation = filter.getOperation();
        nestedQuery.addFilters(filter.getFilters(), query);
        return nestedQuery;
    }
}
