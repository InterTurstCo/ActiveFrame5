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
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
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

        when(collectionsService.findCollection(anyString(), any(SortOrder.class), anyListOf(Filter.class), anyInt(), anyInt()))
                .thenReturn(new GenericIdentifiableObjectCollection());

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

        when(collectionsService.findCollection(anyString(), any(SortOrder.class), anyListOf(Filter.class), anyInt(), anyInt()))
                .thenReturn(new GenericIdentifiableObjectCollection());

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

        when(collectionsService.findCollection(anyString(), any(SortOrder.class), anyListOf(Filter.class), anyInt(), anyInt()))
                .thenReturn(new GenericIdentifiableObjectCollection());

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

    // CMFIVE-5387 workaround tests
    @Test
    public void testPartialFetch_DeficientIds() {
        testALotOfIds(5555, 5000);
    }

    @Test
    public void testPartialFetch_ExcessIds() {
        testALotOfIds(5432, 1500);
    }

    @Test
    public void testPartialFetch_ExactSplit() {
        testALotOfIds(5000, NamedCollectionRetriever.MAX_IDS_PER_QUERY);
    }

    // CMFIVE-7227 test
    @Test
    public void testPartialFetch_Unlimited() {
        testALotOfIds(7718, 0);
    }

    private void testALotOfIds(int idCount, int maxResults) {
        /*SolrDocument doc = mock(SolrDocument.class);
        when(doc.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenAnswer(new Answer<Object>() {
            int seqNum = 0;
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return Integer.toString(++seqNum);
            }
        });*/
        SolrDocumentList docList = new SolrDocumentList();
        for (int i = 0; i < idCount; ++i) {
            SolrDocument doc = mock(SolrDocument.class);
            when(doc.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn(Integer.toString(i + 1));
            when(doc.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
            docList.add(doc);
        }

        when(idService.createId(anyString())).thenAnswer(new Answer<Id>() {
            @Override
            public Id answer(InvocationOnMock invocation) throws Throwable {
                String id = invocation.getArgumentAt(0, String.class);
                return new RdbmsId(55, Integer.valueOf(id));
            }
        });

        int expectedResults = idCount;
        if (maxResults > 0 && maxResults < expectedResults) {
            expectedResults = maxResults;
        }

        final int expectedCalls = (idCount + NamedCollectionRetriever.MAX_IDS_PER_QUERY - 1)
                / NamedCollectionRetriever.MAX_IDS_PER_QUERY;
        final int[] expectedFilterSize = new int[expectedCalls];
        for (int i = 0; i < expectedCalls; ++i) {
            expectedFilterSize[i] = Math.min(NamedCollectionRetriever.MAX_IDS_PER_QUERY,
                    idCount - i * NamedCollectionRetriever.MAX_IDS_PER_QUERY);
        }

        when(collectionsService.findCollection(anyString(), any(SortOrder.class), anyListOf(Filter.class), anyInt(), anyInt()))
                .thenAnswer(new Answer<IdentifiableObjectCollection>() {
                    int callNum = 0;
                    @Override
                    public IdentifiableObjectCollection answer(InvocationOnMock invocation) throws Throwable {
                        List<?> filters = invocation.getArgumentAt(2, List.class);
                        assertEquals(1, filters.size());
                        assertEquals(filters.get(0).getClass(), IdsIncludedFilter.class);
                        IdsIncludedFilter filter = (IdsIncludedFilter) filters.get(0);
                        int maxResults = invocation.getArgumentAt(4, Integer.class);

                        assertThat(callNum, Matchers.lessThan(expectedCalls));
                        assertEquals(expectedFilterSize[callNum], filter.getCriterionKeys().size());
                        ++callNum;

                        IdentifiableObjectCollection coll = new GenericIdentifiableObjectCollection();
                        ReferenceFieldConfig idConfig = new ReferenceFieldConfig();
                        idConfig.setName("id");
                        coll.setFieldsConfiguration(Arrays.<FieldConfig>asList(idConfig));
                        for (int i = 0; i < filter.getCriterionKeys().size(); ++i) {
                            try {
                                RdbmsId id = (RdbmsId) filter.getCriterion(i).get();
                                coll.setId(coll.size(), id);
                                if (coll.size() == maxResults) {
                                    break;
                                }
                            } catch (Exception e) {
                                System.err.println("Error processing iteration #" + i);
                                e.printStackTrace();
                            }
                        }
                        return coll;
                    }
                });

        NamedCollectionRetriever retriever = new NamedCollectionRetriever("TestCollection");
        initRetriever(retriever);
        IdentifiableObjectCollection result = retriever.queryCollection(docList, maxResults);

        assertEquals(expectedResults, result.size());
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
