package ru.intertrust.cm.core.business.impl.search;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.util.SpringApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class NamedCollectionRetrieverTest {

    @Mock private CollectionsService collectionsService;
    @Mock private IdService idService;

    @Captor private ArgumentCaptor<List<Filter>> queryFilters;

    @Before
    public void initContext() {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getAutowireCapableBeanFactory()).thenAnswer(RETURNS_MOCKS);
        new SpringApplicationContext().setApplicationContext(appCtx);
    }

    @Test
    public void testSimple() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id1");
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id3");
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(doc1, doc2, doc3));

        Id id1 = mock(Id.class);
        when(idService.createId("id1")).thenReturn(id1);
        Id id2 = mock(Id.class);
        when(idService.createId("id2")).thenReturn(id2);
        Id id3 = mock(Id.class);
        when(idService.createId("id3")).thenReturn(id3);

        NamedCollectionRetriever retriever = new NamedCollectionRetriever("TestCollection");
        initRetriever(retriever);
        retriever.queryCollection(docList, 20);
        // Не сохраняем и не проверяем результат, т.к. возвращается только то, что было получено от collectionsService

        verify(collectionsService).findCollection(eq("TestCollection"), any(SortOrder.class), queryFilters.capture(),
                eq(0), eq(20));
        assertEquals(1, queryFilters.getValue().size());
        assertThat(queryFilters.getValue(), hasItem(isA(IdsIncludedFilter.class)));
        IdsIncludedFilter filter = (IdsIncludedFilter) queryFilters.getValue().get(0);
        assertThat(filter, new IdsFilterMatcher(id1, id2, id3));
    }

    @Test
    public void testCustomFilters() {
        SolrDocument docMock = mock(SolrDocument.class);
        when(docMock.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id");
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(docMock, docMock, docMock, docMock, docMock));
        when(idService.createId(anyString())).thenAnswer(RETURNS_MOCKS);

        Filter filterMock = mock(Filter.class);
        NamedCollectionRetriever retriever = new NamedCollectionRetriever("TestCollection",
                Arrays.asList(filterMock, filterMock));
        initRetriever(retriever);
        retriever.queryCollection(docList, 20);
        // Не сохраняем и не проверяем результат, т.к. возвращается только то, что было получено от collectionsService

        verify(collectionsService).findCollection(eq("TestCollection"), any(SortOrder.class), queryFilters.capture(),
                eq(0), eq(20));
        assertEquals(3, queryFilters.getValue().size());
        assertThat(queryFilters.getValue(), hasItem(isA(IdsIncludedFilter.class)));
        for (Filter filter : queryFilters.getValue()) {
            if (filter instanceof IdsIncludedFilter) {
                assertEquals(5, filter.getCriterionKeys().size());      // количество найденных ID
            }
        }
    }

    @Test
    public void testMergedFilter() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id1");
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id3");
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(doc1, doc2, doc3));

        Id id1 = mock(Id.class);
        when(idService.createId("id1")).thenReturn(id1);
        Id id2 = mock(Id.class);
        when(idService.createId("id2")).thenReturn(id2);
        Id id3 = mock(Id.class);
        when(idService.createId("id3")).thenReturn(id3);
        Id id4 = mock(Id.class);
        when(idService.createId("id4")).thenReturn(id4);

        Filter filterMock = mock(Filter.class);
        IdsIncludedFilter idsFilter = new IdsIncludedFilter(
                new ReferenceValue(id2), new ReferenceValue(id3), new ReferenceValue(id4));
        NamedCollectionRetriever retriever = new NamedCollectionRetriever("TestCollection",
                Arrays.asList(filterMock, idsFilter));
        initRetriever(retriever);
        retriever.queryCollection(docList, 20);
        // Не сохраняем и не проверяем результат, т.к. возвращается только то, что было получено от collectionsService

        verify(collectionsService).findCollection(eq("TestCollection"), any(SortOrder.class), queryFilters.capture(),
                eq(0), eq(20));
        assertEquals(2, queryFilters.getValue().size());
        assertThat(queryFilters.getValue(), allOf(hasItem(isA(IdsIncludedFilter.class)), hasItem(filterMock)));
        for (Filter filter : queryFilters.getValue()) {
            if (filter instanceof IdsIncludedFilter) {
                assertThat((IdsIncludedFilter) filter, new IdsFilterMatcher(id2, id3));
            }
        }
    }

    private void initRetriever(NamedCollectionRetriever retriever) {
        ReflectionTestUtils.setField(retriever, "collectionsService", collectionsService);
        ReflectionTestUtils.setField(retriever, "idService", idService);
    }

    private static class IdsFilterMatcher extends BaseMatcher<IdsIncludedFilter> {

        private Id[] expected;

        public IdsFilterMatcher(Id... expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object item) {
            if (!(item instanceof IdsIncludedFilter)) {
                return false;
            }
            IdsIncludedFilter filter = (IdsIncludedFilter) item;
            List<Id> actual = new IdsIncludedFilterCopy(filter).getIds();
            if (expected.length != actual.size()) {
                return false;
            }
            for (Id id : expected) {
                if (!hasItem(id).matches(actual)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            // TODO Auto-generated method stub
        }
    }

    private static class IdsIncludedFilterCopy extends IdsIncludedFilter {

        public IdsIncludedFilterCopy(IdsIncludedFilter original) {
            super(original);
        }

        @SuppressWarnings("rawtypes")
        public List<Id> getIds() {
            ArrayList<Id> ids = new ArrayList<>(parameterMap.size());
            for (List<Value> criterion : parameterMap.values()) {
                ReferenceValue value = (ReferenceValue) criterion.get(0);
                ids.add(value.get());
            }
            return ids;
        }
    }
}
