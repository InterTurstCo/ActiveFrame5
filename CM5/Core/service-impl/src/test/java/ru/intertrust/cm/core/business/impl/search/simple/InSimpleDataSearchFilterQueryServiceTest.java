package ru.intertrust.cm.core.business.impl.search.simple;

import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.business.api.simpledata.InSimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InSimpleDataSearchFilterQueryServiceTest {

    private static final String FIELD_NAME = "someField";
    private static final String PREFIX = "solr";

    private InSimpleDataSearchFilterQueryService service;

    @Before
    public void setUp() {
        SimpleSearchUtils searchUtils = mock(SimpleSearchUtils.class);
        when(searchUtils.getSolrFieldName(any(SimpleDataConfig.class), eq(FIELD_NAME))).thenReturn(PREFIX + FIELD_NAME);
        service = new InSimpleDataSearchFilterQueryService(searchUtils);
    }

    @Test
    public void getType() {
        assertEquals("The type must be InSimpleDataSearchFilter.class", InSimpleDataSearchFilter.class, service.getType());
    }

    @Test
    public void prepareQuery_StringValue() {
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new InSimpleDataSearchFilter(FIELD_NAME, new StringValue("SOME_STRING")));
        assertEquals(PREFIX + FIELD_NAME + ":(\"SOME_STRING\")", string);
    }

    @Test
    public void prepareQuery_LongValue() {
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new InSimpleDataSearchFilter(FIELD_NAME, new LongValue(1L)));
        assertEquals(PREFIX + FIELD_NAME + ":(1)", string);
    }

    @Test
    public void prepareQuery_BooleanValue() {
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new InSimpleDataSearchFilter(FIELD_NAME, new BooleanValue(true)));
        assertEquals(PREFIX + FIELD_NAME + ":(true)", string);
    }

    @Test
    public void prepareQuery_DateTimeValue() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new InSimpleDataSearchFilter(FIELD_NAME, new DateTimeValue(date)));
        assertEquals(PREFIX + FIELD_NAME + ":(\"" + sdf.format(date) + "\")", string);
    }

    @Test
    public void prepareQuery_ListValue() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String expected = PREFIX + FIELD_NAME + ":(\"SOME STRING\" OR 1 OR true OR \"" + sdf.format(date) + "\")";
        ListValue listValue = new ListValue(new StringValue("SOME STRING"), new LongValue(1L),
                new BooleanValue(true), new DateTimeValue(date));
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class),
                        new InSimpleDataSearchFilter(FIELD_NAME, listValue));
        assertEquals(expected, string);
    }

    @Test
    public void prepareQuery_List() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String expected = PREFIX + FIELD_NAME + ":(\"SOME\\\"STRING\" OR 1 OR true OR \"" + sdf.format(date) + "\")";
        List<Value<?>> listValue = new ArrayList<>(4);
        listValue.add(new StringValue("SOME\"STRING"));
        listValue.add(new LongValue(1L));
        listValue.add(new BooleanValue(true));
        listValue.add(new DateTimeValue(date));
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class),
                        new InSimpleDataSearchFilter(FIELD_NAME, listValue));
        assertEquals(expected, string);
    }

    @Test
    public void prepareQuery_NullOrEmptyList() {
        String expected = PREFIX + FIELD_NAME + ":(\"\")";
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class),
                        new InSimpleDataSearchFilter(FIELD_NAME, (Value)null));
        assertEquals(expected, string);
        string = service
                .prepareQuery(mock(SimpleDataConfig.class),
                        new InSimpleDataSearchFilter(FIELD_NAME, (ListValue)null));
        assertEquals(expected, string);
        string = service
                .prepareQuery(mock(SimpleDataConfig.class),
                        new InSimpleDataSearchFilter(FIELD_NAME, new ListValue()));
        assertEquals(expected, string);
        string = service
                .prepareQuery(mock(SimpleDataConfig.class),
                        new InSimpleDataSearchFilter(FIELD_NAME, new ArrayList<Value<?>>(0)));
        assertEquals(expected, string);
    }

}