package ru.intertrust.cm.core.business.impl.search.simple;

import org.junit.Before;
import org.junit.Test;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.simpledata.EqualSimpleDataSearchFilter;
import ru.intertrust.cm.core.business.api.simpledata.LikeSimpleDataSearchFilter;
import ru.intertrust.cm.core.config.SimpleDataConfig;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleDataSearchFilterQueryFactoryImplTest {

    private SimpleDataSearchFilterQueryFactoryImpl factory;

    @Before
    public void setUp() {

        EqualsSimpleDataSearchFilterQueryService eq = mock(EqualsSimpleDataSearchFilterQueryService.class);
        LikeSimpleDataSearchFilterQueryService like = mock(LikeSimpleDataSearchFilterQueryService.class);

        when(eq.prepareQuery(any(SimpleDataConfig.class), any(EqualSimpleDataSearchFilter.class))).thenReturn("eq_query");
        doReturn(EqualSimpleDataSearchFilter.class).when(eq).getType();

        when(like.prepareQuery(any(SimpleDataConfig.class), any(LikeSimpleDataSearchFilter.class))).thenReturn("like_query");
        doReturn(LikeSimpleDataSearchFilter.class).when(like).getType();

        factory = new SimpleDataSearchFilterQueryFactoryImpl(asList(eq, like));
    }

    @Test
    public void getQuery_eq() {
        String string = factory
                .getQuery(mock(SimpleDataConfig.class), new EqualSimpleDataSearchFilter("some_field", new StringValue("")));
        assertEquals("eq_query", string);
    }

    @Test
    public void getQuery_like() {
        String string = factory
                .getQuery(mock(SimpleDataConfig.class), new LikeSimpleDataSearchFilter("some_field", new StringValue("")));
        assertEquals("like_query", string);
    }
}