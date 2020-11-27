package ru.intertrust.cm.core.business.impl.search;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.OneOfListFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

@RunWith(MockitoJUnitRunner.class)
public class OneOfListFilterAdapterTest {

    @InjectMocks private OneOfListFilterAdapter adapter = new OneOfListFilterAdapter();

    @Mock private SearchConfigHelper configHelper;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(adapter);
    }

    @Test
    public void testSingleValue() {
        Id id = mock(Id.class);
        when(id.toStringRepresentation()).thenReturn("id001");
        OneOfListFilter filter = new OneOfListFilter("TestField", Arrays.asList(new ReferenceValue(id)));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.REF)));
        SearchQuery query = mock(SearchQuery.class);
        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_r_testfield:(id001)", result);
    }

    @Test
    public void testMultipleValues() {
        Id id1 = mock(Id.class);
        when(id1.toStringRepresentation()).thenReturn("id001");
        Id id2 = mock(Id.class);
        when(id2.toStringRepresentation()).thenReturn("id002");
        Id id3 = mock(Id.class);
        when(id3.toStringRepresentation()).thenReturn("id003");
        OneOfListFilter filter = new OneOfListFilter("TestField",
                Arrays.asList(new ReferenceValue(id1), new ReferenceValue(id2), new ReferenceValue(id3)));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.REF)));
        SearchQuery query = mock(SearchQuery.class);
        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_r_testfield:(id001 OR id002 OR id003)", result);
    }

    @Test
    public void testNoValues() {
        OneOfListFilter filter = new OneOfListFilter("TestField");
        SearchQuery query = mock(SearchQuery.class);
        String result = adapter.getFilterString(filter, query);
        assertNull(result);
    }
}
