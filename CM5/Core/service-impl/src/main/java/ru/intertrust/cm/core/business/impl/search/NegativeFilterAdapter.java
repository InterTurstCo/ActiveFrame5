package ru.intertrust.cm.core.business.impl.search;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.NegativeFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

public class NegativeFilterAdapter implements FilterAdapter<NegativeFilter> {

    @Autowired
    private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String getFilterString(NegativeFilter filter, SearchQuery query) {
        SearchFilter nested = filter.getBaseFilter();
        FilterAdapter adapter = searchFilterImplementorFactory.createImplementorFor(nested.getClass());
        String source = adapter.getFilterString(nested, query);
        if (source.startsWith("-")) {
            return source.substring(1);
        }
        if (source.startsWith("NOT ")) {
            return source.substring(4);
        }
        if (source.startsWith("(")) {
            return "NOT " + source;
        }
        return "-" + source;
    }

}
