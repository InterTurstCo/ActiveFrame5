package ru.intertrust.cm.core.business.impl.search;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ru.intertrust.cm.core.business.api.dto.CombiningFilter;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.NegativeFilter;
import ru.intertrust.cm.core.business.api.dto.NumberRangeFilter;
import ru.intertrust.cm.core.business.api.dto.OneOfListFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;
import ru.intertrust.cm.core.business.api.dto.TimeIntervalFilter;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;

@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PrepareForTest({SearchServiceImpl.class})
public class SearchServiceTest {

    @Mock private SolrServer solrServer;
    @Mock private ImplementorFactory<SearchFilter, FilterAdapter<? extends SearchFilter>> searchFilterImplementorFactory;
    @Mock private SearchConfigHelper configHelper;
    @Mock private NamedCollectionRetriever namedCollectionRetriever;
    @Mock private QueryCollectionRetriever queryCollectionRetriever;

    @InjectMocks private SearchServiceImpl service = new SearchServiceImpl();

    @InjectMocks private NegativeFilterAdapter negAdapter = new NegativeFilterAdapter();
    @InjectMocks private CombiningFilterAdapter combAdapter = new CombiningFilterAdapter();

    //@Captor private ArgumentCaptor<List<Filter>> filters;

    @Before
    public void initSearchService() throws Exception {
        Field resultsLimit = SearchServiceImpl.class.getDeclaredField("RESULTS_LIMIT");
        resultsLimit.setAccessible(true);
        resultsLimit.set(service, 5000);
    }

    @Test
    public void testSimpleSearch_Basic() throws Exception {
        // Подготовка данных
        QueryResponse response = mock(QueryResponse.class);
        when(solrServer.query(any(SolrParams.class))).thenReturn(response);
        SolrDocument docMock = mock(SolrDocument.class);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(docMock, docMock, docMock, docMock, docMock));
        when(response.getResults()).thenReturn(docList);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);
        IdentifiableObjectCollection objects = mock(IdentifiableObjectCollection.class);
        when(objects.size()).thenReturn(5);
        when(namedCollectionRetriever.queryCollection(docList, 20)).thenReturn(objects);
        //when(idService.createId(anyString())).thenAnswer(RETURNS_MOCKS);

        // Вызов проверяемого метода
        service.search("test search", "TestArea", "TestCollection", 20);

        // Проверка правильности запроса к Solr
        ArgumentCaptor<SolrQuery> params = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(params.capture());
        assertEquals("cm_text:(test search) OR cm_content:(test search)", params.getValue().getQuery());
        assertThat(params.getValue().getFilterQueries(), allOf(
                hasItemInArray("cm_area:\"TestArea\"")
                ));
        assertEquals("cm_main,score", params.getValue().getFields());
        assertEquals(20, params.getValue().getRows().intValue());
    }

    @Test
    public void testSimpleSearch_MultiLanguage() throws Exception {
        // Подготовка данных
        QueryResponse response = mock(QueryResponse.class);
        when(solrServer.query(any(SolrParams.class))).thenReturn(response);
        SolrDocument docMock = mock(SolrDocument.class);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(docMock, docMock, docMock, docMock));
        when(response.getResults()).thenReturn(docList);
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList("ru", "en", "de"));

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);
        IdentifiableObjectCollection objects = mock(IdentifiableObjectCollection.class);
        when(objects.size()).thenReturn(5);
        when(namedCollectionRetriever.queryCollection(docList, 10)).thenReturn(objects);

        // Вызов проверяемого метода
        service.search("test search", "TestArea", "TestCollection", 10);

        // Проверка правильности запроса к Solr
        ArgumentCaptor<SolrQuery> params = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(params.capture());
        assertEquals("cm_text_ru:(test search) OR cm_content_ru:(test search) "
                + "OR cm_text_en:(test search) OR cm_content_en:(test search) "
                + "OR cm_text_de:(test search) OR cm_content_de:(test search)", params.getValue().getQuery());
        assertThat(params.getValue().getFilterQueries(), allOf(
                hasItemInArray("cm_area:\"TestArea\"")
                ));
        assertEquals("cm_main,score", params.getValue().getFields());
        assertEquals(10, params.getValue().getRows().intValue());
    }

    @Test
    public void testSimpleSearch_CyclicQueries() throws Exception {
        // Подготовка данных
        QueryResponse response = mock(QueryResponse.class);
        when(solrServer.query(any(SolrParams.class))).thenReturn(response);
        SolrDocument docMock = mock(SolrDocument.class);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(docMock, docMock, docMock, docMock, docMock));
        when(response.getResults()).thenReturn(docList);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);

        IdentifiableObjectCollection partResult = mock(IdentifiableObjectCollection.class);
        when(partResult.size()).thenReturn(2);
        IdentifiableObjectCollection fullResult = mock(IdentifiableObjectCollection.class);
        when(fullResult.size()).thenReturn(5);
        when(namedCollectionRetriever.queryCollection(same(docList), anyInt()))
                .thenReturn(partResult, fullResult);

        // Вызов проверяемого метода
        service.search("test search", "TestArea", "TestCollection", 5);

        // Проверка правильности запросов к Solr
        ArgumentCaptor<SolrQuery> params = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer, times(2)).query(params.capture());
        // Поскольку при повторных вызовах используется один и тот же объект SolrQuery,
        // значения, переданные в первый вызов, не сохраняются
        assertEquals("cm_text:(test search) OR cm_content:(test search)", params.getValue().getQuery());
        assertThat(params.getValue().getFilterQueries(), allOf(
                hasItemInArray("cm_area:\"TestArea\"")
                ));
        assertEquals("cm_main,score", params.getValue().getFields());
        assertEquals(5 * 2 * 5 / 2, params.getValue().getRows().intValue());
    }

    @Test
    public void testExtendedSearch_Basic() throws Exception {
        // Подготовка данных
        SearchQuery query = new SearchQuery();
        query.addAreas(Arrays.asList("Area1", "Area2", "Area3"));
        query.setTargetObjectTypes(Arrays.asList("TargetType1", "TargetType2"));
        query.addFilter(new TextSearchFilter("StringField", "text search"));
        ReferenceValue refMock = mock(ReferenceValue.class);
        query.addFilter(new OneOfListFilter("ReferenceField", Arrays.asList(refMock, refMock)));
        query.addFilter(new NumberRangeFilter("LongField", 15, 25));
        query.addFilter(new TimeIntervalFilter("DateField", null, new Date()));
        FilterAdapter<SearchFilter> adapterMock = mock(FilterAdapter.class);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory.createImplementorFor(any(Class.class)))
                .thenReturn(adapterMock);
        when(adapterMock.getFilterString(argThat(isA(TextSearchFilter.class)), any(SearchQuery.class)))
                .thenReturn("<text filter>");
        when(adapterMock.getFilterString(argThat(isA(OneOfListFilter.class)), any(SearchQuery.class)))
                .thenReturn("<reference filter>");
        when(adapterMock.getFilterString(argThat(isA(NumberRangeFilter.class)), any(SearchQuery.class)))
                .thenReturn("<number filter>");
        when(adapterMock.getFilterString(argThat(isA(TimeIntervalFilter.class)), any(SearchQuery.class)))
                .thenReturn("<date filter>");
        IndexedDomainObjectConfig configMock = mock(IndexedDomainObjectConfig.class);
        when(configMock.getType()).thenReturn("TargetType");

        when(configHelper.findApplicableTypes("LongField", Arrays.asList("Area1", "Area2", "Area3"), Arrays.asList("TargetType1", "TargetType2"))).
                thenReturn(Arrays.asList("TargetType"));
        when(configHelper.findApplicableTypes("DateField", Arrays.asList("Area1", "Area2", "Area3"), Arrays.asList("TargetType1", "TargetType2"))).
                thenReturn(Arrays.asList("TargetType"));
        when(configHelper.findApplicableTypes("StringField", Arrays.asList("Area1", "Area2", "Area3"), Arrays.asList("TargetType1", "TargetType2"))).
                thenReturn(Arrays.asList("TargetType"));
        when(configHelper.findApplicableTypes("ReferenceField", Arrays.asList("Area1", "Area2", "Area3"), Arrays.asList("TargetType1", "TargetType2"))).
                thenReturn(Arrays.asList("TargetType"));

        QueryResponse response = mock(QueryResponse.class);
        when(solrServer.query(any(SolrParams.class))).thenReturn(response);
        SolrDocument docMock = mock(SolrDocument.class);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(docMock, docMock, docMock, docMock));
        when(response.getResults()).thenReturn(docList);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);
        IdentifiableObjectCollection objects = mock(IdentifiableObjectCollection.class);
        when(objects.size()).thenReturn(4);
        when(namedCollectionRetriever.queryCollection(docList, 20)).thenReturn(objects);

        // Вызов проверяемого метода
        service.search(query, "TestCollection", 20);

        // Проверка правильности запроса к Solr
        ArgumentCaptor<SolrQuery> params = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(params.capture());
        assertThat(params.getValue().getQuery(), allOf(
                containsString("<text filter>"),
                containsString("<reference filter>"),
                containsString("<number filter>"),
                containsString("<date filter>")));
        assertEquals("<> AND <> AND <> AND <>", params.getValue().getQuery().replaceAll("<[^>]+>", "<>"));
        assertThat(params.getValue().getFilterQueries(), allOf(
                hasItemInArray("cm_area:(\"Area1\" OR \"Area2\" OR \"Area3\")"),
                hasItemInArray("cm_type:(\"TargetType1\" OR \"TargetType2\")"),
                hasItemInArray("cm_item:\"TargetType\"")
                ));
        assertEquals("cm_main,score", params.getValue().getFields());
        assertEquals(20, params.getValue().getRows().intValue());
    }

    @Test
    public void testExtendedSearch_CollectionFilters() throws Exception {
        // Подготовка данных
        SearchQuery query = new SearchQuery();
        query.addAreas(Arrays.asList("Area1", "Area2", "Area3"));
        query.setTargetObjectType("TargetType");
        query.addFilter(new TextSearchFilter("StringField", "text search"));
        FilterAdapter<SearchFilter> adapterMock = mock(FilterAdapter.class);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory.createImplementorFor(any(Class.class)))
                .thenReturn(adapterMock);
        when(adapterMock.getFilterString(argThat(isA(TextSearchFilter.class)), any(SearchQuery.class)))
                .thenReturn("<text filter>");
        IndexedDomainObjectConfig configMock = mock(IndexedDomainObjectConfig.class);
        when(configMock.getType()).thenReturn("TargetType");

        when(configHelper.findApplicableTypes("StringField", Arrays.asList("Area1", "Area2", "Area3"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("TargetType"));

        QueryResponse response = mock(QueryResponse.class);
        when(solrServer.query(any(SolrParams.class))).thenReturn(response);
        SolrDocument docMock = mock(SolrDocument.class);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(docMock, docMock, docMock, docMock));
        when(response.getResults()).thenReturn(docList);

        Filter filterMock = mock(Filter.class);
        List<Filter> filters = Arrays.asList(filterMock, filterMock);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection", filters)
                .thenReturn(namedCollectionRetriever);
        IdentifiableObjectCollection objects = mock(IdentifiableObjectCollection.class);
        when(objects.size()).thenReturn(4);
        when(namedCollectionRetriever.queryCollection(docList, 20)).thenReturn(objects);

        // Вызов проверяемого метода
        service.search(query, "TestCollection", filters, 20);

        // Проверка правильности запроса к Solr
        ArgumentCaptor<SolrQuery> params = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer).query(params.capture());
        assertThat(params.getValue().getQuery(), containsString("<text filter>"));
        assertEquals("<text filter>", params.getValue().getQuery());
        assertThat(params.getValue().getFilterQueries(), allOf(
                hasItemInArray("cm_area:(\"Area1\" OR \"Area2\" OR \"Area3\")"),
                hasItemInArray("cm_type:(\"TargetType\")"),
                hasItemInArray("cm_item:\"TargetType\"")
                ));
        assertEquals("cm_main,score", params.getValue().getFields());
        assertEquals(20, params.getValue().getRows().intValue());
    }

    @Test
    public void testExtendedSearch_MergeResults() throws Exception {
        // Подготовка данных
        SearchQuery query = new SearchQuery();
        query.addAreas(Arrays.asList("Area"));
        query.setTargetObjectType("TargetType");
        query.addFilter(new TextSearchFilter("RootField", "root object's field search"));
        query.addFilter(new TextSearchFilter("LinkedField", "linked object's field search"));

        FilterAdapter<SearchFilter> adapterMock = mock(FilterAdapter.class);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory.createImplementorFor(any(Class.class)))
                .thenReturn(adapterMock);
        when(adapterMock.getFilterString(argThat(isA(TextSearchFilter.class)), any(SearchQuery.class)))
                .thenReturn("<text filter>");

        IndexedDomainObjectConfig targetObjectFieldConfig = mock(IndexedDomainObjectConfig.class);
        when(targetObjectFieldConfig.getType()).thenReturn("TargetType");
        IndexedDomainObjectConfig linkedObjectFieldConfig = mock(IndexedDomainObjectConfig.class);
        when(linkedObjectFieldConfig.getType()).thenReturn("LinkedType");

        when(configHelper.findApplicableTypes("LinkedField", Arrays.asList("Area"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("LinkedType"));
        when(configHelper.findApplicableTypes("RootField", Arrays.asList("Area"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("TargetType"));

        SolrDocument a1Doc = docMock("doc1", 1f);
        SolrDocument a2Doc = docMock("doc3", 0.77f);
        SolrDocument a3Doc = docMock("doc5", 0.5f);
        SolrDocument a4Doc = docMock("doc6", 0.5f);
        SolrDocument a5Doc = docMock("doc7", 0.1234f);
        SolrDocumentList aDocList = new SolrDocumentList();
        aDocList.addAll(Arrays.asList(a1Doc, a2Doc, a3Doc, a4Doc, a5Doc));
        aDocList.setMaxScore(1f);
        QueryResponse aResponse = mock(QueryResponse.class);
        when(aResponse.getResults()).thenReturn(aDocList);
        SolrDocument b1Doc = docMock("doc2", 0.9f);
        SolrDocument b2Doc = docMock("doc1", 0.8f);
        SolrDocument b3Doc = docMock("doc4", 0.55f);
        SolrDocument b4Doc = docMock("doc7", 0.5f);
        SolrDocument b5Doc = docMock("doc8", 0.333f);
        SolrDocumentList bDocList = new SolrDocumentList();
        bDocList.addAll(Arrays.asList(b1Doc, b2Doc, b3Doc, b4Doc, b5Doc));
        bDocList.setMaxScore(0.9f);
        QueryResponse bResponse = mock(QueryResponse.class);
        when(bResponse.getResults()).thenReturn(bDocList);
        when(solrServer.query(any(SolrParams.class))).thenReturn(aResponse, bResponse);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);
        IdentifiableObjectCollection objects = mock(IdentifiableObjectCollection.class);
        when(objects.size()).thenReturn(5);
        when(namedCollectionRetriever.queryCollection(any(SolrDocumentList.class), eq(20))).thenReturn(objects);

        // Вызов проверяемого метода
        service.search(query, "TestCollection", 20);

        // Проверка правильности запросов к Solr
        ArgumentCaptor<SolrQuery> params = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer, times(2)).query(params.capture());
        SolrQuery solrQuery1 = params.getAllValues().get(0);
        assertEquals("<text filter>", solrQuery1.getQuery());
        assertThat(solrQuery1.getFilterQueries(), allOf(
                hasItemInArray("cm_area:(\"Area\")"),
                hasItemInArray("cm_type:(\"TargetType\")"),
                hasItemInArray("cm_item:\"TargetType\"")
                ));
        assertEquals("cm_main,score", solrQuery1.getFields());
        assertEquals(20, solrQuery1.getRows().intValue());
        SolrQuery solrQuery2 = params.getAllValues().get(1);
        assertEquals("<text filter>", solrQuery2.getQuery());
        assertThat(solrQuery2.getFilterQueries(), allOf(
                hasItemInArray("cm_area:(\"Area\")"),
                hasItemInArray("cm_type:(\"TargetType\")"),
                hasItemInArray("cm_item:\"LinkedType\"")
                ));
        assertEquals("cm_main,score", solrQuery2.getFields());
        assertEquals(20, solrQuery2.getRows().intValue());

        // Проверка правильности запроса к сервису коллекций
        ArgumentCaptor<SolrDocumentList> docList = ArgumentCaptor.forClass(SolrDocumentList.class);
        verify(namedCollectionRetriever).queryCollection(docList.capture(), eq(20));
        HashSet<String> expectedIds = new HashSet<>(Arrays.asList( "doc1", "doc7" ));
        assertEquals(expectedIds.size(), docList.getValue().size());
        for (SolrDocument doc : docList.getValue()) {
            String id = (String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID);
            assertTrue(expectedIds.contains(id));
            expectedIds.remove(id);
        }
    }

    // CMFIVE-5093 test
    @Test
    public void testExtendedSearch_MergeClippedResults() throws Exception {
        // Подготовка данных
        SearchQuery query = new SearchQuery();
        query.addAreas(Arrays.asList("Area"));
        query.setTargetObjectType("TargetType");
        query.addFilter(new TextSearchFilter("RootField", "root object's field search"));
        query.addFilter(new TextSearchFilter("LinkedField", "linked object's field search"));

        FilterAdapter<SearchFilter> adapterMock = mock(FilterAdapter.class);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory.createImplementorFor(any(Class.class)))
                .thenReturn(adapterMock);
        when(adapterMock.getFilterString(argThat(isA(TextSearchFilter.class)), any(SearchQuery.class)))
                .thenReturn("<text filter>");

        IndexedDomainObjectConfig targetObjectFieldConfig = mock(IndexedDomainObjectConfig.class);
        when(targetObjectFieldConfig.getType()).thenReturn("TargetType");
        IndexedDomainObjectConfig linkedObjectFieldConfig = mock(IndexedDomainObjectConfig.class);
        when(linkedObjectFieldConfig.getType()).thenReturn("LinkedType");

        when(configHelper.findApplicableTypes("LinkedField", Arrays.asList("Area"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("LinkedType"));
        when(configHelper.findApplicableTypes("RootField", Arrays.asList("Area"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("TargetType"));

        SolrDocument a1Doc = docMock("doc1", 10f);
        SolrDocument a2Doc = docMock("doc2", 7f);
        SolrDocument a3Doc = docMock("doc3", 5f);
        SolrDocument a4Doc = docMock("doc4", 5f);
        SolrDocument a5Doc = docMock("doc5", 2f);
        SolrDocument a6Doc = docMock("doc6", 1f);
        SolrDocument a7Doc = docMock("doc7", 1f);
        SolrDocument a8Doc = docMock("doc8", 1f);
        SolrDocumentList aPartialList = new SolrDocumentList();
        aPartialList.addAll(Arrays.asList(a1Doc, a2Doc, a3Doc));
        aPartialList.setNumFound(8);
        aPartialList.setMaxScore(10f);
        QueryResponse aPartialResponse = mock(QueryResponse.class);
        when(aPartialResponse.getResults()).thenReturn(aPartialList);
        SolrDocumentList aFullList = new SolrDocumentList();
        aFullList.addAll(Arrays.asList(a1Doc, a2Doc, a3Doc, a4Doc, a5Doc, a6Doc, a7Doc, a8Doc));
        aFullList.setNumFound(8);
        aFullList.setMaxScore(10f);
        QueryResponse aFullResponse = mock(QueryResponse.class);
        when(aFullResponse.getResults()).thenReturn(aFullList);

        SolrDocument b1Doc = docMock("docA", 8f);
        SolrDocument b2Doc = docMock("doc7", 4f);
        SolrDocument b3Doc = docMock("doc2", 2f);
        SolrDocument b4Doc = docMock("docB", 1f);
        SolrDocumentList bPartialList = new SolrDocumentList();
        bPartialList.addAll(Arrays.asList(b1Doc, b2Doc, b3Doc));
        bPartialList.setNumFound(4);
        bPartialList.setMaxScore(8f);
        QueryResponse bPartialResponse = mock(QueryResponse.class);
        when(bPartialResponse.getResults()).thenReturn(bPartialList);
        SolrDocumentList bFullList = new SolrDocumentList();
        bFullList.addAll(Arrays.asList(b1Doc, b2Doc, b3Doc, b4Doc));
        bFullList.setNumFound(4);
        bFullList.setMaxScore(8f);
        QueryResponse bFullResponse = mock(QueryResponse.class);
        when(bFullResponse.getResults()).thenReturn(bFullList);
        when(solrServer.query(any(SolrParams.class)))
                .thenReturn(aPartialResponse, bPartialResponse, aFullResponse, bFullResponse);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);
        IdentifiableObjectCollection objects = mock(IdentifiableObjectCollection.class);
        when(objects.size()).thenReturn(3);
        when(namedCollectionRetriever.queryCollection(any(SolrDocumentList.class), anyInt())).thenReturn(objects);

        // Вызов проверяемого метода
        service.search(query, "TestCollection", 3);

        // Проверка правильности запроса к сервису коллекций - по сути, проверка правильности объединённого списка
        ArgumentCaptor<SolrDocumentList> docList = ArgumentCaptor.forClass(SolrDocumentList.class);
        verify(namedCollectionRetriever).queryCollection(docList.capture(), eq(3));
        HashSet<String> expectedIds = new HashSet<>(Arrays.asList( "doc2", "doc7" ));
        assertEquals(expectedIds.size(), docList.getValue().size());
        for (SolrDocument doc : docList.getValue()) {
            String id = (String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID);
            assertTrue(expectedIds.contains(id));
            expectedIds.remove(id);
        }
    }

    @Test    // CMFIVE-9635 test
    public void testExtendedSearch_CompositeFilters() throws Exception {
        // Подготовка данных
        SearchQuery query = new SearchQuery();
        query.addAreas(Arrays.asList("TestArea"));
        query.setTargetObjectType("TargetType");
        Id idAMock = mock(Id.class);
        when(idAMock.toStringRepresentation()).thenReturn("id_A");
        Id idBMock = mock(Id.class);
        when(idBMock.toStringRepresentation()).thenReturn("id_B");
        Calendar dateEnd = Calendar.getInstance();
        dateEnd.set(2017, 4, 5, 10, 30);
        query.addFilter(new TextSearchFilter("StringField", "text search"));
        query.addFilter(new NegativeFilter(new CombiningFilter(CombiningFilter.Op.OR,
                new OneOfListFilter("ReferenceField", Arrays.asList(new ReferenceValue(idAMock), new ReferenceValue(idBMock))),
                new TimeIntervalFilter("DateField", null, dateEnd.getTime()))));

        FilterAdapter<SearchFilter> adapterMock = mock(FilterAdapter.class);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory
                .createImplementorFor(TextSearchFilter.class)).thenReturn(adapterMock);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory
                .createImplementorFor(OneOfListFilter.class)).thenReturn(adapterMock);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory
                .createImplementorFor(TimeIntervalFilter.class)).thenReturn(adapterMock);
        when(adapterMock.getFilterString(argThat(isA(TextSearchFilter.class)), any(SearchQuery.class)))
                .thenReturn("<text filter>");
        when(adapterMock.getFilterString(argThat(isA(OneOfListFilter.class)), any(SearchQuery.class)))
                .thenReturn("<reference filter>");
        when(adapterMock.getFilterString(argThat(isA(TimeIntervalFilter.class)), any(SearchQuery.class)))
                .thenReturn("<date filter>");
        // Поскольку адаптеры композитных фильтров работают в тесном взаимодействии с классом SearchServiceImpl,
        // для этих фильтров используются реальные адаптеры
        when((FilterAdapter<NegativeFilter>)searchFilterImplementorFactory
                .createImplementorFor(NegativeFilter.class)).thenReturn(negAdapter);
        when((FilterAdapter<CombiningFilter>)searchFilterImplementorFactory
                .createImplementorFor(CombiningFilter.class)).thenReturn(combAdapter);

        IndexedDomainObjectConfig configMock = mock(IndexedDomainObjectConfig.class);
        when(configMock.getType()).thenReturn("TargetType");

        when(configHelper.findApplicableTypes("DateField", Arrays.asList("TestArea"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("TargetType"));
        when(configHelper.findApplicableTypes("StringField", Arrays.asList("TestArea"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("TargetType"));
        when(configHelper.findApplicableTypes("ReferenceField", Arrays.asList("TestArea"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("TargetType"));

        // Возвращаются пустые наборы документов, т.к. нужно проверить только правильность запросов к Solr
        QueryResponse response = mock(QueryResponse.class);
        when(solrServer.query(any(SolrParams.class))).thenReturn(response);
        SolrDocumentList docList = new SolrDocumentList();
        when(response.getResults()).thenReturn(docList);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);

        // Вызов проверяемого метода
        service.search(query, "TestCollection", 20);

        // Проверка правильности запросов к Solr
        ArgumentCaptor<SolrQuery> params = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer, times(2)).query(params.capture());
        String simpleQuery = null;
        String compositeQuery = null;
        for (SolrQuery q : params.getAllValues()) {
            if (q.getQuery().startsWith("-(")) {
                compositeQuery = q.getQuery();
            } else {
                simpleQuery = q.getQuery();
            }
            assertThat(q.getFilterQueries(), allOf(
                    hasItemInArray("cm_area:(\"TestArea\")"),
                    hasItemInArray("cm_type:(\"TargetType\")"),
                    hasItemInArray("cm_item:\"TargetType\"")
                    ));
            assertEquals("cm_main,score", q.getFields());
            assertEquals(20, q.getRows().intValue());
        }
        assertEquals("<text filter>", simpleQuery);
        assertTrue(compositeQuery.matches("^-\\(<.+> OR <.+>\\)$"));
        assertTrue(compositeQuery.contains("<reference filter>"));
        assertTrue(compositeQuery.contains("<date filter>"));
    }

    @Test   // CMFIVE-15653 test
    public void testExtendedSearch_RepeatedIds() throws Exception {
        // Подготовка данных
        SearchQuery query = new SearchQuery();
        query.addAreas(Arrays.asList("TestArea"));
        query.setTargetObjectType("TargetType");
        query.addFilter(new TextSearchFilter("StringField", "text search"));
        FilterAdapter<SearchFilter> adapterMock = mock(FilterAdapter.class);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory.createImplementorFor(any(Class.class)))
                .thenReturn(adapterMock);
        when(adapterMock.getFilterString(argThat(isA(TextSearchFilter.class)), any(SearchQuery.class)))
                .thenReturn("<text filter>");
        IndexedDomainObjectConfig configMock = mock(IndexedDomainObjectConfig.class);
        when(configMock.getType()).thenReturn("TargetType");

        when(configHelper.findApplicableTypes("StringField", Arrays.asList("TestArea"), Arrays.asList("TargetType"))).
                thenReturn(Arrays.asList("TargetType"));

        SolrDocument doc1 = docMock("doc1", 1f);
        SolrDocument doc2 = docMock("doc2", 0.77f);
        SolrDocument doc3 = docMock("doc3", 0.5f);
        SolrDocument doc2copy = docMock("doc2", 0.45f);
        SolrDocument doc4 = docMock("doc4", 0.1234f);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(doc1, doc2, doc3, doc2copy, doc4));
        docList.setMaxScore(1f);
        QueryResponse response = mock(QueryResponse.class);
        when(response.getResults()).thenReturn(docList);

        when(solrServer.query(any(SolrParams.class))).thenReturn(response);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);
        IdentifiableObjectCollection objects = mock(IdentifiableObjectCollection.class);
        when(objects.size()).thenReturn(4);
        when(namedCollectionRetriever.queryCollection(docList, 20)).thenReturn(objects);

        // Вызов проверяемого метода
        service.search(query, "TestCollection", 20);

        // Проверка правильности запроса к Solr
        ArgumentCaptor<SolrDocumentList> compactedList = ArgumentCaptor.forClass(SolrDocumentList.class);
        verify(namedCollectionRetriever).queryCollection(compactedList.capture(), eq(20));
        assertEquals(4, compactedList.getValue().size());
        assertTrue(!compactedList.getValue().contains(doc2copy));
    }

    @Test    // CMFIVE-25472 test
    public void testExtendedSearch_MultipleEverywhere() throws Exception {
        // Подготовка данных
        SearchQuery query = new SearchQuery();
        query.addAreas(Arrays.asList("TestArea"));
        query.setTargetObjectType("TargetType");
        query.addFilter(new CombiningFilter(CombiningFilter.AND,
                new TextSearchFilter(SearchFilter.EVERYWHERE, "one query"),
                new TextSearchFilter(SearchFilter.EVERYWHERE, "another query")));

        FilterAdapter<SearchFilter> adapterMock = mock(FilterAdapter.class);
        when((FilterAdapter<SearchFilter>)searchFilterImplementorFactory
                .createImplementorFor(TextSearchFilter.class)).thenReturn(adapterMock);
        when(adapterMock.getFilterString(argThat(isA(TextSearchFilter.class)), any(SearchQuery.class)))
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        TextSearchFilter filter = invocation.getArgumentAt(0, TextSearchFilter.class);
                        return "<" + filter.getText() + ">";
                    }
                });
        when(configHelper.findApplicableTypes(eq(SearchFilter.EVERYWHERE), any(List.class), eq(Arrays.asList("TargetType"))))
                .thenReturn(Collections.singleton(SearchConfigHelper.ALL_TYPES));
        // Поскольку адаптеры композитных фильтров работают в тесном взаимодействии с классом SearchServiceImpl,
        // для этих фильтров используются реальные адаптеры
        when((FilterAdapter<CombiningFilter>)searchFilterImplementorFactory
                .createImplementorFor(CombiningFilter.class)).thenReturn(combAdapter);

        IndexedDomainObjectConfig configMock = mock(IndexedDomainObjectConfig.class);
        when(configMock.getType()).thenReturn("TargetType");

        // Возвращаются пустые наборы документов, т.к. нужно проверить только правильность запросов к Solr
        QueryResponse response = mock(QueryResponse.class);
        when(solrServer.query(any(SolrParams.class))).thenReturn(response);
        SolrDocumentList docList = new SolrDocumentList();
        when(response.getResults()).thenReturn(docList);

        whenNew(NamedCollectionRetriever.class).withArguments("TestCollection").thenReturn(namedCollectionRetriever);

        // Вызов проверяемого метода
        service.search(query, "TestCollection", 20);

        // Проверка правильности запросов к Solr
        ArgumentCaptor<SolrQuery> params = ArgumentCaptor.forClass(SolrQuery.class);
        verify(solrServer, times(2)).query(params.capture());
        ArrayList<String> queries = new ArrayList<>(Arrays.asList("<one query>", "<another query>"));
        for (SolrQuery q : params.getAllValues()) {
            assertThat(q.getFilterQueries(), allOf(
                    hasItemInArray("cm_area:(\"TestArea\")"),
                    hasItemInArray("cm_type:(\"TargetType\")")
                    ));
            assertEquals("cm_main,score", q.getFields());
            assertEquals(20, q.getRows().intValue());
            assertTrue(queries.contains(q.getQuery()));
            queries.remove(q.getQuery());
        }
    }
/*
    @Test
    public void tempTestCombine() {
        SolrDocument docA1 = docMock("doc1", 1f);
        SolrDocument docA2 = docMock("doc3", 0.77f);
        SolrDocument docA3 = docMock("doc5", 0.5f);
        SolrDocument docA4 = docMock("doc6", 0.5f);
        SolrDocument docA5 = docMock("doc7", 0.1234f);
        SolrDocumentList listA = new SolrDocumentList();
        listA.addAll(Arrays.asList(docA1, docA2, docA3, docA4, docA5));
        listA.setMaxScore(1f);
        SolrDocument docB1 = docMock("doc2", 0.9f);
        SolrDocument docB2 = docMock("doc1", 0.8f);
        SolrDocument docB3 = docMock("doc4", 0.55f);
        SolrDocument docB4 = docMock("doc7", 0.5f);
        SolrDocument docB5 = docMock("doc8", 0.333f);
        SolrDocumentList listB = new SolrDocumentList();
        listB.addAll(Arrays.asList(docB1, docB2, docB3, docB4, docB5));
        listB.setMaxScore(0.9f);

        SolrDocumentList list = service.intersectResults(Arrays.asList(listA, listB));
        assertEquals(2, list.size());
        assertEquals(docA1, list.get(0));
        assertEquals(docB4, list.get(1));
        /*SolrDocumentList list = service.unionResults(Arrays.asList(listA, listB));
        assertEquals(8, list.size());
        assertEquals(docA1, list.get(0));
        assertEquals(docB1, list.get(1));
        assertEquals(docA2, list.get(2));
        assertEquals(docB3, list.get(3));
        assertEquals(docA3, list.get(4));
        assertEquals(docB4, list.get(5));
        assertEquals(docA4, list.get(6));
        assertEquals(docB5, list.get(7));* /
    }
*/
    private SolrDocument docMock(String id, float score) {
        SolrDocument doc = mock(SolrDocument.class);
        when(doc.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn(id);
        when(doc.getFieldValue("score")).thenReturn(score);
        return doc;
    }

    public static Matcher<SolrDocument> matches(final String id, final float score) {
        return new BaseMatcher<SolrDocument>() {
            @Override
            public boolean matches(Object obj) {
                SolrDocument doc = (SolrDocument) obj;
                return id.equals(doc.getFieldValue(SolrFields.MAIN_OBJECT_ID))
                        && score == (Float) doc.getFieldValue("score");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Document ID " + id + ", score=" + score);
            }
        };
    }
}
