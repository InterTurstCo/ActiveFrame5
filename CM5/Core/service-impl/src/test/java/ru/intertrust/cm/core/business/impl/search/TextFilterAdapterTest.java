package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;

public class TextFilterAdapterTest {

    @Mock
    SearchConfigHelper configHelper;

    @InjectMocks
    TextFilterAdapter adapter = new TextFilterAdapter();

    @Before
    public void init() {
        initMocks(this);
    }

//    @Test
    public void testComplexStringAndTwoLanguages() {
        TextSearchFilter filter = new TextSearchFilter("TestField",
                "find WoRdS && part* || \"whole phrase\" +required -excess escape:semicolon");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("Area1", "Area2"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList("ru", "en"));

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

//    @Test
    public void testSearchContent() {
        TextSearchFilter filter = new TextSearchFilter(TextSearchFilter.CONTENT, "\"Test phrase\"");
        SearchQuery query = mock(SearchQuery.class);
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList("uk"));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_content_uk:(\"Test phrase\")", result);
    }
}
