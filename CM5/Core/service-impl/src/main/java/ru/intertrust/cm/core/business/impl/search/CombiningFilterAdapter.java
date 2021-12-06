package ru.intertrust.cm.core.business.impl.search;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.CombiningFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.impl.search.SearchServiceImpl;

import java.util.ArrayList;
import java.util.List;

public class CombiningFilterAdapter implements CompositeFilterAdapter<CombiningFilter> {

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Override
    public String getFilterString(CombiningFilter filter, SearchQuery query) {
        if (filter.getFilters().size() > 1) {
            throw new IllegalArgumentException("This method must not be called for composite filters");
        }

        if (filter.getFilters().size() == 0) {
            return null;
        }
        SearchFilter nestedFilter = filter.getFilters().get(0);
        @SuppressWarnings("unchecked")
        FilterAdapter<SearchFilter> adapter = (FilterAdapter<SearchFilter>)
                searchFilterImplementorFactory.createImplementorFor(nestedFilter.getClass());
        return adapter.getFilterString(nestedFilter, query);
    }

    @Override
    public boolean isCompositeFilter(CombiningFilter filter) {
        if (filter.getFilters().size() == 1) {
            SearchFilter nestedFilter = filter.getFilters().get(0);
            @SuppressWarnings("unchecked")
            FilterAdapter<SearchFilter> adapter = (FilterAdapter<SearchFilter>)
                    searchFilterImplementorFactory.createImplementorFor(nestedFilter.getClass());
            return adapter.isCompositeFilter(nestedFilter);
        } else {
            return filter.getFilters().size() > 1;
        }
    }

    @Override
    public List<String> getFieldNames(CombiningFilter filter, SearchQuery query) {
        if (filter.getFilters().size() > 1) {
            throw new IllegalArgumentException("This method must not be called for composite filters");
        }

        if (filter.getFilters().size() == 0) {
            return new ArrayList<>(0);
        }
        SearchFilter nestedFilter = filter.getFilters().get(0);
        @SuppressWarnings("unchecked")
        FilterAdapter<SearchFilter> adapter = (FilterAdapter<SearchFilter>)
                searchFilterImplementorFactory.createImplementorFor(nestedFilter.getClass());
        return adapter.getFieldNames(nestedFilter, query);
    }

    @Override
    public SearchServiceImpl.QueryProcessor processCompositeFilter(CombiningFilter filter,
            SearchServiceImpl.QueryProcessor queryProcessor, SearchQuery query) {
        SearchServiceImpl.QueryProcessor nestedQuery = queryProcessor.newNestedQuery();
        nestedQuery.setCombineOperation(filter.getOperation());
        nestedQuery.addFilters(filter.getFilters(), query);
        return nestedQuery;
    }
}
