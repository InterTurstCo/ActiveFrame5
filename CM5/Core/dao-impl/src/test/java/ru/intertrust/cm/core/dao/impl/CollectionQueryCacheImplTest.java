package ru.intertrust.cm.core.dao.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.impl.CollectionQueryCacheImpl.CollectionQueryKey;

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

        CollectionQueryKey key = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, null, accessToken);
        CollectionQueryKey anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, null, accessToken);

        assertTrue(key.equals(anotherKey));
        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, null, accessToken);
        assertTrue(key.equals(anotherKey));

        sortOrder = createByNameSortOrder();
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, null, accessToken);
        assertTrue(key.equals(anotherKey));

        sortOrder = createByIdSortOrder();
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, null, accessToken);
        assertFalse(key.equals(anotherKey));

        filter = new Filter();
        filter.setFilter("byDepartment");
        filter.addCriterion(0, new ReferenceValue(new RdbmsId(2, 2)));
        filter.addCriterion(1, new ReferenceValue(new RdbmsId(2, 2)));
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, null, accessToken);
        assertFalse(key.equals(anotherKey));
        
        Set<ListValue> listParams = new HashSet<>();
        
        List<Value> params1 = new ArrayList<>();
        params1.add(new StringValue("a"));
        params1.add(new StringValue("b"));
        List<Value> params2 = new ArrayList<>();
        params1.add(new StringValue("c"));
        params1.add(new StringValue("d"));
        
        ListValue listValue1 = new ListValue(params1);
        ListValue listValue2 = new ListValue(params2);
        
        listParams.add(listValue1);
        listParams.add(listValue2);
        
        key = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, listParams, accessToken);
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, listParams, accessToken);
        assertTrue(key.equals(anotherKey));
        
        listParams = new HashSet<>();
        listParams.add(listValue2);
        listParams.add(listValue1);
        anotherKey = new CollectionQueryKey("Employees", Collections.singletonList(filter), sortOrder, 2, 0, listParams, accessToken);
        assertTrue(key.equals(anotherKey));            
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
