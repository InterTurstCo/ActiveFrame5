package ru.intertrust.cm.core.business.impl.search.simple;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.simpledata.EqualSimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EqualsSimpleDataSearchFilterQueryServiceTest {

    private static final String FIELD_NAME = "someField";
    private static final String PREFIX = "solr";

    private EqualsSimpleDataSearchFilterQueryService service;

    @Before
    public void setUp() {
        SimpleSearchUtils searchUtils = mock(SimpleSearchUtils.class);
        when(searchUtils.getSolrFieldName(any(SimpleDataConfig.class), eq(FIELD_NAME))).thenReturn(PREFIX + FIELD_NAME);
        service = new EqualsSimpleDataSearchFilterQueryService(searchUtils);
    }

    @Test
    public void getType() {
        assertEquals("The type must be EqualSimpleDataSearchFilter.class", EqualSimpleDataSearchFilter.class, service.getType());
    }

    @Test
    public void prepareQuery_StringValue() {
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new EqualSimpleDataSearchFilter(FIELD_NAME, new StringValue("SOME_STRING")));
        assertEquals(PREFIX + FIELD_NAME + ": \"SOME_STRING\"", string);
        string = service
                .prepareQuery(mock(SimpleDataConfig.class), new EqualSimpleDataSearchFilter(FIELD_NAME, new StringValue("SOME STRING")));
        assertEquals(PREFIX + FIELD_NAME + ": \"SOME STRING\"", string);
        string = service
                .prepareQuery(mock(SimpleDataConfig.class), new EqualSimpleDataSearchFilter(FIELD_NAME, new StringValue("SOME\"STRING")));
        assertEquals(PREFIX + FIELD_NAME + ": \"SOME\\\"STRING\"", string);
    }

    @Test
    public void prepareQuery_LongValue() {
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new EqualSimpleDataSearchFilter(FIELD_NAME, new LongValue(1L)));
        assertEquals(PREFIX + FIELD_NAME + ": 1", string);
    }

    @Test
    public void prepareQuery_BooleanValue() {
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new EqualSimpleDataSearchFilter(FIELD_NAME, new BooleanValue(true)));
        assertEquals(PREFIX + FIELD_NAME + ": true", string);
    }

    @Test
    public void prepareQuery_DateTimeValue() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new EqualSimpleDataSearchFilter(FIELD_NAME, new DateTimeValue(date)));
        assertEquals(PREFIX + FIELD_NAME + ": \"" + sdf.format(date) + "\"", string);
    }

    @Test
    @Ignore //TODO Уточни, месяц выводится не верно
    public void prepareQuery_TimelessDateValue() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String string = service
                .prepareQuery(mock(SimpleDataConfig.class),
                        new EqualSimpleDataSearchFilter(FIELD_NAME, new TimelessDateValue(date, TimeZone.getDefault())));
        assertEquals(PREFIX + FIELD_NAME + ": \"" + sdf.format(date) + "\"", string);
    }
}