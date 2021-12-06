package ru.intertrust.cm.core.dao.impl;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterCriteriaConfig;
import ru.intertrust.cm.core.config.base.CollectionFilterReferenceConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CollectionQueryInitializerImplTest {

    @Mock
    private ConfigurationExplorer configurationExplorer;
    @Mock
    private UserGroupGlobalCache userGroupCache;
    @Mock
    private CurrentUserAccessor currentUserAccessor;
    @Mock
    private DomainObjectQueryHelper domainObjectQueryHelper;
    @Mock
    private AccessToken accessToken;

    @InjectMocks
    private CollectionQueryInitializerImpl collectionQueryInitializer;

    @Before
    public void setup() {
        when(accessToken.isDeferred()).thenReturn(false);
    }

    @Test
    public void initializeQuery() {

        String prototype = "select\n" +
                "                    e.id, e.name, e.position ::r-placeholder_2\n" +
                "                from\n" +
                "                    Employee e\n" +
                "                     ::reference-placeholder\n" +
                "                where\n" +
                "                    1=1 ::criteria-placeholder\n";

        String expectedQuery = "SELECT e.id, e.name, e.position, p.id FROM Employee e JOIN person p USING (id) WHERE 1 = 1 AND (p.name = 'ABC')";

        Filter filter = mock(Filter.class);
        when(filter.getFilter()).thenReturn("filter-name");
        Filter filter2 = mock(Filter.class);
        when(filter2.getFilter()).thenReturn("filter-name2");

        CollectionFilterConfig collectionFilterConfig = mock(CollectionFilterConfig.class);
        when(collectionFilterConfig.getName()).thenReturn("filter-name");

        CollectionFilterConfig collectionFilterConfig2 = mock(CollectionFilterConfig.class);
        when(collectionFilterConfig2.getName()).thenReturn("filter-name2");

        CollectionFilterCriteriaConfig srcFilterCriteria = mock(CollectionFilterCriteriaConfig.class);
        when(collectionFilterConfig.getFilterCriteria()).thenReturn(srcFilterCriteria);
        when(srcFilterCriteria.getValue()).thenReturn("p.name = 'ABC'");
        when(srcFilterCriteria.getPlaceholder()).thenReturn("criteria-placeholder");

        CollectionFilterReferenceConfig filterReference = mock(CollectionFilterReferenceConfig.class);
        when(collectionFilterConfig.getFilterReference()).thenReturn(filterReference);
        when(filterReference.getValue()).thenReturn("join person p using(id)");
        when(filterReference.getPlaceholder()).thenReturn("reference-placeholder");

        CollectionFilterReferenceConfig filterReference2 = mock(CollectionFilterReferenceConfig.class);
        when(collectionFilterConfig2.getFilterReference()).thenReturn(filterReference2);
        when(filterReference2.getValue()).thenReturn(", p.id");
        when(filterReference2.getPlaceholder()).thenReturn("r-placeholder_2");

        CollectionConfig collectionConfig = mock(CollectionConfig.class);
        when(collectionConfig.getFilters()).thenReturn(Arrays.asList(collectionFilterConfig, collectionFilterConfig2));
        when(collectionConfig.getPrototype()).thenReturn(prototype);

        String actualQuery = collectionQueryInitializer.initializeQuery(collectionConfig, Arrays.asList(filter, filter2), null, 0, 0, accessToken);

        assertEquals(expectedQuery, actualQuery);
    }
}