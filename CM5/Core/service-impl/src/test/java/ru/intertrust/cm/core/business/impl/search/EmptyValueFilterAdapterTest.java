package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.EmptyValueFilter;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

@RunWith(MockitoJUnitRunner.class)
public class EmptyValueFilterAdapterTest {

    @Mock SearchConfigHelper configHelper;

    @InjectMocks EmptyValueFilterAdapter adapter = new EmptyValueFilterAdapter();

    @Test
    @SuppressWarnings("unchecked")
    public void testLongField() {
        SearchConfigHelper.FieldDataType type = new SearchConfigHelper.FieldDataType(FieldType.LONG);
        when(configHelper.getFieldTypes(eq("TestField"), anyCollection())).thenReturn(Collections.singleton(type));
        SearchQuery query = mock(SearchQuery.class);

        EmptyValueFilter filter = new EmptyValueFilter("TestField");
        String result = adapter.getFilterString(filter, query);
        assertEquals("-cm_l_testfield:[\"\" TO *]", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDifferentTypeField() {
        SearchConfigHelper.FieldDataType typeRef = new SearchConfigHelper.FieldDataType(FieldType.REFERENCE);
        SearchConfigHelper.FieldDataType typeMDate = new SearchConfigHelper.FieldDataType(FieldType.DATETIME, true);
        SearchConfigHelper.FieldDataType typeText = new SearchConfigHelper.FieldDataType(FieldType.TEXT);
        SearchConfigHelper.FieldDataType typeMText = new SearchConfigHelper.FieldDataType(FieldType.TEXT, true);
        when(configHelper.getFieldTypes(eq("TestField"), anyCollection())).thenReturn(
                new LinkedHashSet<>(Arrays.asList(typeRef, typeMDate, typeText, typeMText)));
        when(configHelper.getSupportedLanguages(eq("TestField"), eq("AreaA"))).thenReturn(Arrays.asList(""));
        when(configHelper.getSupportedLanguages(eq("TestField"), eq("AreaB"))).thenReturn(
                Arrays.asList("ru", "en"), Arrays.asList("ru", "fr"));
        SearchQuery query = mock(SearchQuery.class);
        when(query.getAreas()).thenReturn(Arrays.asList("AreaA", "AreaB"));

        EmptyValueFilter filter = new EmptyValueFilter("TestField");
        String result = adapter.getFilterString(filter, query);
        assertTrue("Поисковый запрос должен иметь вид NOT (zzzz OR zzzz ...),"
                + " где каждый zzzz - выражение вида cm_xx_testfield:[\"\" TO *]", result.matches(
                "NOT \\((cm_[a-z]+_testfield:\\[\"\" TO \\*\\] OR ){7}cm_[a-z]+_testfield:\\[\"\" TO \\*\\]\\)"));
        assertTrue(result.contains("cm_r_testfield:"));
        assertTrue(result.contains("cm_dts_testfield:"));
        assertTrue(result.contains("cm_t_testfield:"));
        assertTrue(result.contains("cm_ru_testfield:"));
        assertTrue(result.contains("cm_en_testfield:"));
        assertTrue(result.contains("cm_ts_testfield:"));
        assertTrue(result.contains("cm_rus_testfield:"));
        assertTrue(result.contains("cm_frs_testfield:"));
    }
}
