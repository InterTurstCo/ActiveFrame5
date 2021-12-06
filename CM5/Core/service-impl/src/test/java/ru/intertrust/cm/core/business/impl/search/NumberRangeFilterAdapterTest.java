package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.NumberRangeFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

@RunWith(MockitoJUnitRunner.class)
public class NumberRangeFilterAdapterTest {

    @Mock SearchConfigHelper configHelper;

    @InjectMocks private NumberRangeFilterAdapter adapter = new NumberRangeFilterAdapter();

    @Test
    @SuppressWarnings("unchecked")
    public void testOpenRange() {
        NumberRangeFilter filter = new NumberRangeFilter("TestField", 15, null);
        when(configHelper.getFieldTypes(eq("TestField"), anyCollection(), anyCollection())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG)));
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("TestArea"));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_l_testfield:[15 TO *]", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleTypes() {
        NumberRangeFilter filter = new NumberRangeFilter("TestField", -50, 1000);
        when(configHelper.getFieldTypes(eq("TestField"), anyCollection(), anyCollection())).thenReturn(Sets.<SearchFieldType>newSet(
                new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG, true),
                new SimpleSearchFieldType(SimpleSearchFieldType.Type.DOUBLE)));
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("TestArea"));

        String result = adapter.getFilterString(filter, query);
        assertEquals("(cm_ls_testfield:[-50 TO 1000] OR cm_d_testfield:[-50 TO 1000])", result);
    }
}
