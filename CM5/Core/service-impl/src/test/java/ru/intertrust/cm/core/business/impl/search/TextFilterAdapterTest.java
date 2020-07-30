package ru.intertrust.cm.core.business.impl.search;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.model.SearchException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
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
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn();
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(
                        new TextSearchFieldType(Arrays.asList("ru", "en"))));

        String result = adapter.getFilterString(filter, query);
        HashSet<String> expectedFields = new HashSet<>(
                Arrays.asList("cm_en_testfield", "cm_ru_testfield", "cm_t_testfield"));
        result = checkAndCutBeginning(result, "(");
        while (true) {
            String foundField = null;
            for (String fieldName : expectedFields) {
                if (result.startsWith(fieldName)) {
                    foundField = fieldName;
                    break;
                }
            }
            assertNotNull("Unexpected field name: " + result, foundField);
            expectedFields.remove(foundField);
            result = result.substring(foundField.length());
            result = checkAndCutBeginning(result,
                    ":(find WoRdS && part* || \"whole phrase\" +required -excess escape\\:semicolon)");
            if (expectedFields.isEmpty()) {
                assertTrue(result.equals(")"));
                break;
            }
            result = checkAndCutBeginning(result, " OR ");
        }
    }

    private String checkAndCutBeginning(String string, String beginning) {
        assertTrue(string.startsWith(beginning));
        return string.substring(beginning.length());
    }

    @Test
    public void testNoLanguage() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "Simple");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("SingleArea"));
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(
                        new TextSearchFieldType(Arrays.asList(""))));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_t_testfield:(Simple)", result);
    }

    @Test
    public void testSearchEverywhere() {
        TextSearchFilter filter = new TextSearchFilter(TextSearchFilter.EVERYWHERE, "Test string");
        SearchQuery query = mock(SearchQuery.class);
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList("ru", "en", "fr"));

        String result = adapter.getFilterString(filter, query);
        assertTrue(result.matches("^\\(cm_text[_\\w]*:\\(\"Test string\"\\)( OR cm_text[_\\w]*:\\(\"Test string\"\\)){3,3}\\)$"));
        assertTrue(result.contains("cm_text:"));
        assertTrue(result.contains("cm_text_ru:"));
        assertTrue(result.contains("cm_text_en:"));
        assertTrue(result.contains("cm_text_fr:"));
    }

    @Test
    public void testSearchContent() {
        TextSearchFilter filter = new TextSearchFilter(TextSearchFilter.CONTENT, "\"Test phrase\"");
        SearchQuery query = mock(SearchQuery.class);
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList("uk"));

        String result = adapter.getFilterString(filter, query);
        assertTrue(result.matches("^\\(cm_content[_\\w]*:\\(\"Test phrase\"\\) OR cm_content[_\\w]*:\\(\"Test phrase\"\\)\\)$"));
        assertTrue(result.contains("cm_content:"));
        assertTrue(result.contains("cm_content_uk:"));
    }

    @Test
    public void testSpecialCharacters() {
        TextSearchFilter filter = new TextSearchFilter("TestField",
                " ( ) [ ] { } : \" \" \\\\ \\( \\) \\[ \\] \\{ \\} \\: \\\" ");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("SingleArea"));
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(
                        new TextSearchFieldType(Arrays.asList(""))));

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
                .thenReturn(Collections.<SearchFieldType>singleton(
                        new TextSearchFieldType(Arrays.asList(""), false, IndexedFieldConfig.SearchBy.SUBSTRING)));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_t_testfield:(\"Quotes must be \\\"quoted\\\"\")", result);
    }

    @Test
    public void testSubstringSearchedField_QuotedString() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "\"Embracing quotes must be removed\"");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("SingleArea"));
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(
                        new TextSearchFieldType(Arrays.asList(""), false, IndexedFieldConfig.SearchBy.SUBSTRING)));

        String result = adapter.getFilterString(filter, query);
        assertEquals("cm_t_testfield:(\"Embracing quotes must be removed\")", result);
    }

    //@Test     Null can not be returned anymore in collection by SearchConfigHelper.getFieldTypes()
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
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(
                        new TextSearchFieldType(Arrays.asList(""))));

        /*String result =*/ adapter.getFilterString(filter, query);
    }

    @Test(expected = SearchException.class)
    public void testTrailingBackslash() {
        TextSearchFilter filter = new TextSearchFilter("TestField", "Finished with backslash \\");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("TestArea"));
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getFieldTypes(anyString(), anyListOf(String.class)))
                .thenReturn(Collections.<SearchFieldType>singleton(
                        new TextSearchFieldType(Arrays.asList(""))));

        /*String result =*/ adapter.getFilterString(filter, query);
    }
}
