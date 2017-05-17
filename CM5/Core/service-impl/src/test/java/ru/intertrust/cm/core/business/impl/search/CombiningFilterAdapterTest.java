package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.CombiningFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

@RunWith(MockitoJUnitRunner.class)
public class CombiningFilterAdapterTest {

    @SuppressWarnings("rawtypes")
    @Mock private ImplementorFactory searchFilterImplementorFactory;

    @InjectMocks private CombiningFilterAdapter adapter = new CombiningFilterAdapter();

    @Mock private CombiningFilter filter;
    @Mock private SearchQuery query;
    @Mock private SearchFilter nestedFilter;
    @Mock private FilterAdapter<SearchFilter> nestedAdapter;

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleNested() {
        when(filter.getFilters()).thenReturn(Arrays.asList(nestedFilter));
        when(searchFilterImplementorFactory.createImplementorFor(nestedFilter.getClass())).thenReturn(nestedAdapter);
        when(nestedAdapter.getFilterString(nestedFilter, query)).thenReturn("nested_filter");

        String result = adapter.getFilterString(filter, query);
        assertEquals("nested_filter", result);
    }
/*
    @Test
    @SuppressWarnings("unchecked")
    public void testTwoNestedOpOr() {
        SearchFilter nestedFilter = mock(SearchFilter.class);
        when(filter.getOperation()).thenReturn(CombiningFilter.OR);
        when(filter.getFilters()).thenReturn(Arrays.asList(nestedFilter, nestedFilter));
        when(searchFilterImplementorFactory.createImplementorFor(nestedFilter.getClass())).thenReturn(nestedAdapter);
        when(nestedAdapter.getFilterString(nestedFilter, query)).thenReturn("nested 1", "nested filter 2");

        String result = adapter.getFilterString(filter, query);
        assertEquals("(nested 1 OR nested filter 2)", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleNestedOpAnd() {
        SearchFilter nestedFilter = mock(SearchFilter.class);
        when(filter.getOperation()).thenReturn(CombiningFilter.AND);
        when(filter.getFilters()).thenReturn(Arrays.asList(nestedFilter, nestedFilter, nestedFilter));
        when(searchFilterImplementorFactory.createImplementorFor(nestedFilter.getClass())).thenReturn(nestedAdapter);
        when(nestedAdapter.getFilterString(nestedFilter, query))
                .thenReturn("simple nested", "NOT negative nested", "(complex nested OR something else)");

        String result = adapter.getFilterString(filter, query);
        assertEquals("(simple nested AND NOT negative nested AND (complex nested OR something else))", result);
    }
*/
}
