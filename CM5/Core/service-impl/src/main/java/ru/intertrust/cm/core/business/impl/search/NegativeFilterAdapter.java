package ru.intertrust.cm.core.business.impl.search;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.NegativeFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.impl.search.SearchServiceImpl.ComplexQuery;

public class NegativeFilterAdapter implements CompositeFilterAdapter<NegativeFilter> {

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Override
    public String getFilterString(NegativeFilter filter, SearchQuery query) {
        FilterAdapter<SearchFilter> adapter = getBaseFilterAdapter(filter);
        String source = adapter.getFilterString(filter.getBaseFilter(), query);
        if (source.startsWith("-")) {
            return source.substring(1);
        }
        // Согласно документации Solr, NOT является не унарным, а бинарным оператором, т.е. запись "NOT a"
        // не имеет смысла (в ней NOT трактуется как искомое слово), а строка "a NOT b" означает "ЕСТЬ a И НЕТ b".
        /*if (source.startsWith("NOT ")) {
            return source.substring(4);
        }
        if (source.startsWith("(")) {
            return "NOT " + source;
        }*/
        return "-" + source;
    }

    @Override
    public boolean isCompositeFilter(NegativeFilter filter) {
        return getBaseFilterAdapter(filter).isCompositeFilter(filter.getBaseFilter());
    }

    @Override
    public SearchServiceImpl.ComplexQuery processCompositeFilter(NegativeFilter filter,
            ComplexQuery queryProcessor, SearchQuery query) {
        CompositeFilterAdapter<SearchFilter> adapter =
                (CompositeFilterAdapter<SearchFilter>) getBaseFilterAdapter(filter);
        SearchServiceImpl.ComplexQuery nestedQuery =
                adapter.processCompositeFilter(filter.getBaseFilter(), queryProcessor, query);
        if (nestedQuery == null) {
            throw new IllegalArgumentException("Filter " + filter.getBaseFilter()
                    + " can't be used inside of NegativeFilter");
        } else {
            nestedQuery.negateResult = true;
            return nestedQuery;
        }
    }

    @SuppressWarnings("unchecked")
    private <F extends SearchFilter> FilterAdapter<F> getBaseFilterAdapter(NegativeFilter filter) {
        return (FilterAdapter<F>) searchFilterImplementorFactory.createImplementorFor(filter.getBaseFilter().getClass());
    }
}
