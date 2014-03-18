package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.NegativeFilter;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

@RunWith(MockitoJUnitRunner.class)
public class NegativeFilterAdapterTest {

    @SuppressWarnings("rawtypes")
    @Mock private ImplementorFactory searchFilterImplementorFactory;

    @InjectMocks
    private NegativeFilterAdapter adapter = new NegativeFilterAdapter();

    @Mock private NegativeFilter filter;
    @Mock private SearchQuery query;
    @Mock private FilterAdapter<SearchFilter> nestedAdapter;

    @Test
    @SuppressWarnings("unchecked")
    public void testSimpleFilter() {
        when(searchFilterImplementorFactory.createImplementorFor(filter.getClass())).thenReturn(nestedAdapter);
        when(nestedAdapter.getFilterString(filter, query)).thenReturn("cm_testfield:(some value)");
        String result = adapter.getFilterString(filter, query);
        assertEquals("-cm_testfield:(some value)", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParenthesedFilter() {
        when(searchFilterImplementorFactory.createImplementorFor(filter.getClass())).thenReturn(nestedAdapter);
        when(nestedAdapter.getFilterString(filter, query)).thenReturn("(cm_fielda:value OR cm_fieldb:12)");
        String result = adapter.getFilterString(filter, query);
        assertEquals("NOT (cm_fielda:value OR cm_fieldb:12)", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSimpleNegativeInside() {
        when(searchFilterImplementorFactory.createImplementorFor(filter.getClass())).thenReturn(nestedAdapter);
        when(nestedAdapter.getFilterString(filter, query)).thenReturn("-some_field:[20 TO *]");
        String result = adapter.getFilterString(filter, query);
        assertEquals("some_field:[20 TO *]", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNegativeInside() {
        when(searchFilterImplementorFactory.createImplementorFor(filter.getClass())).thenReturn(nestedAdapter);
        when(nestedAdapter.getFilterString(filter, query)).thenReturn("NOT (some_field:some condition)");
        String result = adapter.getFilterString(filter, query);
        assertEquals("(some_field:some condition)", result);
    }
}
