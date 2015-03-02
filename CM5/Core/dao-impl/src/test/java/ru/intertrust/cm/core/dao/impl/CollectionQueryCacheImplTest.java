package ru.intertrust.cm.core.dao.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.impl.CollectionQueryCacheImpl.CollectionQueryKey;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CollectionQueryCacheImplTest {

    @Test
    public void testCollectionQueryKeyOnEquals() {
        Filter filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(1, 2)));
        AccessToken accessToken = createMockAccessToken();
        SortOrder sortOrder = createByNameSortOrder();
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(1, 2)));

        CollectionQueryKey key = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, accessToken);
        CollectionQueryKey anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, accessToken);

        assertTrue(key.equals(anotherKey));
        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, accessToken);
        assertTrue(key.equals(anotherKey));

        sortOrder = createByNameSortOrder();
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, accessToken);
        assertTrue(key.equals(anotherKey));

        sortOrder = createByIdSortOrder();
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, accessToken);
        assertFalse(key.equals(anotherKey));

        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));
        filter.addCriterion(1, new ReferenceValue(new RdbmsId(2, 2)));
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, accessToken);
        assertFalse(key.equals(anotherKey));
    }
    
    private AccessToken createMockAccessToken() {
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.isDeferred()).thenReturn(true);

        UserSubject subject = mock(UserSubject.class);
        when(subject.getUserId()).thenReturn(1);
        when(accessToken.getSubject()).thenReturn(subject);
        return accessToken;
    }
    
    private SortOrder createByNameSortOrder() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.name", SortCriterion.Order.ASCENDING));
        return sortOrder;
    }

    private SortOrder createByIdSortOrder() {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion("e.id", SortCriterion.Order.ASCENDING));
        return sortOrder;
    }

}
