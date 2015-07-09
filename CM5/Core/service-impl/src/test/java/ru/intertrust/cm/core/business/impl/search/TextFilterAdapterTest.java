package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;
import ru.intertrust.cm.core.model.SearchException;

public class TextFilterAdapterTest {

    @Mock
    SearchConfigHelper configHelper;

    @InjectMocks
    TextFilterAdapter adapter = new TextFilterAdapter();

    @Before
    public void init() {
        initMocks(this);
    }

    @Test
    public void testComplexStringAndTwoLanguages() {
        TextSearchFilter filter = new TextSearchFilter("TestField",
                "find WoRdS && part* || \"whole phrase\" +required -excess escape:semicolon");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("Area1", "Area2"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList("ru", "en"));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.singleton(SearchFieldType.TEXT));

        String result = adapter.getFilterString(filter, query);
        assertEquals("(cm_en_testfield:(find WoRdS && part* || \"whole phrase\" +required -excess escape\\:semicolon)"
                + " OR cm_ru_testfield:(find WoRdS && part* || \"whole phrase\" +required -excess escape\\:semicolon))",
                result);
    }

    @Test
    public void testNoLanguage() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "Simple");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("SingleArea"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.singleton(SearchFieldType.TEXT));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_t_testfield:(Simple)", result);
    }

    @Test
    public void testSearchEverywhere() {
        TextSearchFilter filter = new TextSearchFilter(TextSearchFilter.EVERYWHERE, "Test string");
        SearchQuery query = mock(SearchQuery.class);
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList("ru", "en", "fr"));

        String result = adapter.getFilterString(filter, query);
        assertEquals("(cm_text_ru:(Test string) OR cm_text_en:(Test string) OR cm_text_fr:(Test string))", result);
    }

    @Test
    public void testSearchContent() {
        TextSearchFilter filter = new TextSearchFilter(TextSearchFilter.CONTENT, "\"Test phrase\"");
        SearchQuery query = mock(SearchQuery.class);
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList("uk"));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_content_uk:(\"Test phrase\")", result);
    }

    @Test
    public void testSpecialCharacters() {
        TextSearchFilter filter = new TextSearchFilter("TestField",
                " ( ) [ ] { } : \" \" \\\\ \\( \\) \\[ \\] \\{ \\} \\: \\\" ");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("SingleArea"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.singleton(SearchFieldType.TEXT));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_t_testfield:( \\( \\) \\[ \\] \\{ \\} \\: \" \" \\\\ \\( \\) \\[ \\] \\{ \\} \\: \\\" )",
                result);
    }

    @Test
    public void testCalculatedField() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "Test search");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("SingleArea"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(null));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_t_testfield:(Test search)", result);
    }

    @Test(expected = SearchException.class)
    public void testUnpairedQuotes() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "Three \"quotes\" in a \"string");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("TestArea"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.singleton(SearchFieldType.TEXT));

        @SuppressWarnings("unused")
        String result = adapter.getFilterString(filter, query);
    }

    @Test(expected = SearchException.class)
    public void testTrailingBackslash() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "Finished with backslash \\");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("TestArea"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.singleton(SearchFieldType.TEXT));

        @SuppressWarnings("unused")
        String result = adapter.getFilterString(filter, query);
    }
}
