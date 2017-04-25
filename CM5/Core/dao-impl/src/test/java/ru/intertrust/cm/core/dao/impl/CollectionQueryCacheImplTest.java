package ru.intertrust.cm.core.dao.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ru.intertrust.cm.core.business.api.FilterForCache;
import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortCriterion.Order;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.CollectionQueryEntry;
import ru.intertrust.cm.core.dao.impl.parameters.ParametersConverter;

public class CollectionQueryCacheImplTest {

    private CollectionQueryCacheImpl cache = new CollectionQueryCacheImpl() {
        @Override
        public Integer getCacheMaxSize() {
            return 1000;
        }
    };
    private CollectionQueryEntry entry = new CollectionQueryEntry("stub", singletonMap("column", (FieldConfig) new ReferenceFieldConfig()));

    @Test
    public void testTrivialEquality() {
        Filter filter = idsFilter("byDepartment", new RdbmsId(1, 1));
        cache.putCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, null, 50, 51, createMockAccessToken(), entry);
        assertSame(entry, cache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, null, 50, 51, createMockAccessToken()));
    }

    private Set<FilterForCache> filtersForCache(List<Filter> filters) {
        HashSet<FilterForCache> filtersForCache = new HashSet<>();
        for (Filter f : filters) {
            filtersForCache.add(new FilterForCache(f));
        }
        return filtersForCache;
    }

    @Test
    public void testFilterValuesDoNotAffectCache() {
        cache.putCollectionQuery("Employees", filtersForCache(singletonList(idsFilter("byDepartment", new RdbmsId(1, 1)))), null, sortOrder("name",
                Order.ASCENDING), 50, 51, createMockAccessToken(), entry);
        assertSame(entry,
                cache.getCollectionQuery("Employees", filtersForCache(singletonList(idsFilter("byDepartment", new RdbmsId(1, 2)))), null, sortOrder("name",
                        Order.ASCENDING), 50, 51, createMockAccessToken()));
    }

    @Test
    public void testFilterListValuesSizeDoAffectCache() {
        ParametersConverter converter = new ParametersConverter();
        List<Filter> filters = singletonList(idsFilter("byDepartment", new RdbmsId(1, 1), new RdbmsId(1, 2)));
        Pair<Map<String, Object>, QueryModifierPrompt> pair = converter.convertReferenceValuesInFilters(filters);
        List<Filter> secondFilters = singletonList(idsFilter("byDepartment", new RdbmsId(1, 1), new RdbmsId(1, 2)));
        Pair<Map<String, Object>, QueryModifierPrompt> secondPair = converter.convertReferenceValuesInFilters(secondFilters);
        cache.putCollectionQuery("Employees", filtersForCache(filters), pair.getSecond(), sortOrder("name",
                Order.ASCENDING), 50, 51, createMockAccessToken(), entry);
        assertNotSame(entry,
                cache.getCollectionQuery("Employees", filtersForCache(secondFilters), secondPair.getSecond(), sortOrder("name",
                        Order.ASCENDING), 50, 51, createMockAccessToken()));
    }

    @Test
    public void testSortOrderAffectCache() {
        Filter filter = idsFilter("byDepartment", new RdbmsId(1, 2));
        cache.putCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, sortOrder("name", Order.ASCENDING), 50, 51,
                createMockAccessToken(), entry);
        assertNotSame(entry,
                cache.getCollectionQuery("Employees", filtersForCache(singletonList(filter)), null, sortOrder("id", Order.ASCENDING), 50, 51,
                        createMockAccessToken()));
    }

    @Test
    public void testFiltersUsedAffectCache() {
        cache.putCollectionQuery("Employees", filtersForCache(asList(idsFilter("byDepartment", new RdbmsId(1, 1)),
                idsFilter("bySuperior", new RdbmsId(2, 2)))), null, sortOrder("name", Order.ASCENDING), 50,
                51, createMockAccessToken(), entry);
        assertNotSame(entry,
                cache.getCollectionQuery("Employees", filtersForCache(singletonList(idsFilter("byDepartment", new RdbmsId(1, 2)))), null, sortOrder("id",
                        Order.ASCENDING), 50, 51, createMockAccessToken()));
    }

    @Test
    public void testCriterionsUsedInFilterAffectCache() {
        cache.putCollectionQuery("Employees", filtersForCache(singletonList(filterWithMultipleCriterions("byDepartment", new ReferenceValue(
                new RdbmsId(1, 2)), new ReferenceValue(new RdbmsId(1, 2))))), null, sortOrder("name", Order.ASCENDING), 50,
                51, createMockAccessToken(), entry);
        assertNotSame(entry,
                cache.getCollectionQuery("Employees", filtersForCache(singletonList(filterWithMultipleCriterions("byDepartment", new ReferenceValue(
                        new RdbmsId(1, 2))))), null, sortOrder("id",
                        Order.ASCENDING), 50, 51, createMockAccessToken()));
    }

    @Test
    public void testCountQuery() {
        cache.putCollectionCountQuery("Employees", filtersForCache(singletonList(filterWithMultipleCriterions("byDepartment", new ReferenceValue(
                new RdbmsId(1, 2)), new ReferenceValue(new RdbmsId(1, 2))))), createMockAccessToken(), entry);
        assertSame(entry,
                cache.getCollectionCountQuery("Employees", filtersForCache(singletonList(filterWithMultipleCriterions("byDepartment", new ReferenceValue(
                        new RdbmsId(1, 2)), new ReferenceValue(new RdbmsId(1, 2))))), createMockAccessToken()));
    }

    @Test
    public void testCountQueryIsNotTheSameAsPlainQuery() {
        cache.putCollectionCountQuery("Employees", filtersForCache(singletonList(filterWithMultipleCriterions("byDepartment", new ReferenceValue(
                new RdbmsId(1, 2)), new ReferenceValue(new RdbmsId(1, 2))))), createMockAccessToken(), entry);
        assertNotSame(entry, cache.getCollectionQuery("Employees", null, null, null, 0, 0, createMockAccessToken()));
    }

    @Test
    public void testUnnamedQuery() {
        cache.putCollectionQuery("select id, name from doc where parent = {0}", 50, 51, null,
                createMockAccessToken(), entry);
        assertSame(entry, cache.getCollectionQuery("select id, name from doc where parent = {0}", 50, 51, null, createMockAccessToken()));
    }

    @Test
    public void testUnnamedQueryListValueSizeAffectsCache() {
        cache.putCollectionQuery("select id, name from doc where parent = {0}", 50, 51, new QueryModifierPrompt().appendIdParamsPrompt("PARAM0", 1),
                createMockAccessToken(), entry);
        assertNotSame(entry, cache.getCollectionQuery("select id, name from doc where parent = {0}", 50, 51,
                new QueryModifierPrompt().appendIdParamsPrompt("PARAM0", 2), createMockAccessToken()));
    }

    private Filter filterWithMultipleCriterions(String name, Value<?>... values) {
        Filter filter = new Filter();
        filter.setFilter(name);
        int i = 0;
        for (Value<?> v : values) {
            filter.addCriterion(i++, v);
        }
        return filter;
    }

    private Filter idsFilter(String name, Id... ids) {
        Filter filter = new Filter();
        filter.setFilter(name);
        Value<?> value = null;
        if (ids.length == 1) {
            value = new ReferenceValue(ids[0]);
        } else {
            List<Value<?>> values = new ArrayList<>();
            for (Id id : ids) {
                values.add(new ReferenceValue(id));
            }
            value = ListValue.createListValue(values);
        }
        filter.addCriterion(0, value);
        return filter;
    }

    private SortOrder sortOrder(String column, Order order) {
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(new SortCriterion(column, order));
        return sortOrder;
    }

    private AccessToken createMockAccessToken() {
        AccessToken accessToken = mock(AccessToken.class);
        when(accessToken.isDeferred()).thenReturn(true);

        UserSubject subject = mock(UserSubject.class);
        when(subject.getUserId()).thenReturn(1);
        when(accessToken.getSubject()).thenReturn(subject);
        return accessToken;
    }

}
