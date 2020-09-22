package ru.intertrust.cm.core.business.impl.search.simple;

import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.simpledata.LikeSimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;
import ru.intertrust.cm.core.model.FatalException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LikeSimpleDataSearchFilterQueryServiceTest {

    private static final String FIELD_NAME = "SomeField";
    private static final String PREFIX = "solr";
    private LikeSimpleDataSearchFilterQueryService service;

    @Before
    public void setUp() {
        SimpleSearchUtils searchUtils = mock(SimpleSearchUtils.class);
        when(searchUtils.getSolrFieldName(any(SimpleDataConfig.class), eq(FIELD_NAME))).thenReturn(PREFIX + FIELD_NAME);
        service = new LikeSimpleDataSearchFilterQueryService(searchUtils);
    }

    @Test
    public void getType() {
        assertEquals("The type must be LikeSimpleDataSearchFilter.class", LikeSimpleDataSearchFilter.class, service.getType());
    }

    @Test
    public void prepareQuery() {
        String string = service
                .prepareQuery(mock(SimpleDataConfig.class), new LikeSimpleDataSearchFilter(FIELD_NAME, new StringValue("SOME_STRING")));
        assertEquals(PREFIX + FIELD_NAME + ": (\"SOME_STRING\")", string);
        string = service
                .prepareQuery(mock(SimpleDataConfig.class), new LikeSimpleDataSearchFilter(FIELD_NAME, new StringValue("SOME STRING")));
        assertEquals(PREFIX + FIELD_NAME + ": (\"SOME STRING\")", string);
        string = service
                .prepareQuery(mock(SimpleDataConfig.class), new LikeSimpleDataSearchFilter(FIELD_NAME, new StringValue("SOME\"STRING")));
        assertEquals(PREFIX + FIELD_NAME + ": (\"SOME\\\"STRING\")", string);
    }

    @Test (expected = FatalException.class)
    public void prepareQuery_illegalValue() {
        service.prepareQuery(mock(SimpleDataConfig.class), new LikeSimpleDataSearchFilter(FIELD_NAME, new LongValue(1L)));
    }
}