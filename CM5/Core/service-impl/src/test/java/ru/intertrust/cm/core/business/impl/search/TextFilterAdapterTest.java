package ru.intertrust.cm.core.business.impl.search;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;
import ru.intertrust.cm.core.model.SearchException;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

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
        if (!result.equals("(cm_en_testfield:(find WoRdS && part* || \"whole phrase\" +required -excess escape\\:semicolon)" +
                " OR cm_ru_testfield:(find WoRdS && part* || \"whole phrase\" +required -excess escape\\:semicolon))") &&
            !result.equals("(cm_ru_testfield:(find WoRdS && part* || \"whole phrase\" +required -excess escape\\:semicolon)" +
                    " OR cm_en_testfield:(find WoRdS && part* || \"whole phrase\" +required -excess escape\\:semicolon))")) {
            assertTrue("Incorrect result: " + result, false);
        }
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
    public void testSubstringSearchedField() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "Quotes must be \"quoted\"");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("SingleArea"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.singleton(SearchFieldType.TEXT_SUBSTRING));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_t_testfield:(\"Quotes must be \\\"quoted\\\"\")", result);
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

        /*String result =*/ adapter.getFilterString(filter, query);
    }

    @Test(expected = SearchException.class)
    public void testTrailingBackslash() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "Finished with backslash \\");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("TestArea"));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.singleton(SearchFieldType.TEXT));

        /*String result =*/ adapter.getFilterString(filter, query);
    }
}
