package ru.intertrust.cm.core.business.impl.search.simple;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.simpledata.BeforeSimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.model.FatalException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BeforeSimpleDataSearchFilterQueryServiceTest {

    private static final String FIELD_NAME = "fieldName";
    private static final String PREFIX = "solr";

    private BeforeSimpleDataSearchFilterQueryService service;

    @Before
    public void init() {
        SimpleSearchUtils searchUtils = mock(SimpleSearchUtils.class);
        when(searchUtils.getSolrFieldName(any(SimpleDataConfig.class), eq(FIELD_NAME))).thenReturn(PREFIX + FIELD_NAME);
        service = new BeforeSimpleDataSearchFilterQueryService(searchUtils);
    }

    @Test
    public void getType() {
        assertEquals("The type must be BeforeSimpleDataSearchFilter.class", BeforeSimpleDataSearchFilter.class, service.getType());
    }

    @Test
    public void prepareQuery_stringArg() {
        String query = service.prepareQuery(
                mock(SimpleDataConfig.class),
                new BeforeSimpleDataSearchFilter(FIELD_NAME, new StringValue("some date"))
        );
        assertEquals(PREFIX + FIELD_NAME + ":[* TO some date]", query);
    }

    @Test
    public void prepareQuery_DateArg() {
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        String query = service.prepareQuery(
                mock(SimpleDataConfig.class),
                new BeforeSimpleDataSearchFilter(FIELD_NAME, new DateTimeValue(now), true)
        );
        assertEquals(PREFIX + FIELD_NAME + ":[* TO " + simpleDateFormat.format(now) + "Z}", query);
    }

    @Test
    public void prepareQuery_DateWithoutTimeArg() {
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String query = service.prepareQuery(
                mock(SimpleDataConfig.class),
                new BeforeSimpleDataSearchFilter(FIELD_NAME, new TimelessDateValue(now, TimeZone.getDefault()), true)
        );
        assertEquals(PREFIX + FIELD_NAME + ":[* TO " + simpleDateFormat.format(now) + "}", query);
    }

    @Test(expected = FatalException.class)
    public void prepareQuery_illegalValue() {
        service.prepareQuery(mock(SimpleDataConfig.class), new BeforeSimpleDataSearchFilter(FIELD_NAME, new LongValue(1L)));
    }
}