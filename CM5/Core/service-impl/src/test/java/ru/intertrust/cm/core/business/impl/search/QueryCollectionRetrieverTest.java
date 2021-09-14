package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import org.springframework.test.util.ReflectionTestUtils;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.impl.search.retrievers.CollectionRetriever;
import ru.intertrust.cm.core.business.impl.search.retrievers.QueryCollectionRetriever;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.util.SpringApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class QueryCollectionRetrieverTest {

    @Mock
    private CollectionsService collectionsService;
    @Mock
    private IdService idService;

    @InjectMocks
    private QueryCollectionRetriever retriever = new QueryCollectionRetriever();

    @Mock
    private Value<?> param1;
    @Mock
    private Value<?> param2;
    private List<? extends Value<?>> parameters = Arrays.asList(param1, param2);
    @InjectMocks
    private QueryCollectionRetriever parametrizedRetriever =
            new QueryCollectionRetriever();

    static {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getAutowireCapableBeanFactory()).thenAnswer(RETURNS_MOCKS);
        new SpringApplicationContext().setApplicationContext(appCtx);
    }

    @Before
    public void init() {
        retriever.setSqlQuery("Test SQL");

        parametrizedRetriever.setSqlParameters(parameters);
        parametrizedRetriever.setSqlQuery("Test SQL");
    }

    @Test
    public void testFullFetch() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id1");
        when(doc1.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(.3f);
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        when(doc2.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id3");
        when(doc3.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(doc2, doc3, doc1));    // mixed

        Id id1 = mock(Id.class);
        when(idService.createId("id1")).thenReturn(id1);
        Id id2 = mock(Id.class);
        when(idService.createId("id2")).thenReturn(id2);
        Id id3 = mock(Id.class);
        when(idService.createId("id3")).thenReturn(id3);

        FieldConfig field1 = mock(FieldConfig.class);
        when(field1.getName()).thenReturn("Id");
        FieldConfig field2 = mock(FieldConfig.class);
        when(field2.getName()).thenReturn("Id");
        IdentifiableObjectCollection sample = collection(Arrays.asList(field1, field2),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1")),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2")),
                pair(id3, new ReferenceValue(id3), new StringValue("object 3"))
                );
        when(collectionsService.findCollectionByQuery(anyString(), anyList(), eq(0), eq(20))).thenReturn(sample);

        IdentifiableObjectCollection result = retriever.queryCollection(docList, 20);

        IdentifiableObjectCollection expected = collection(Arrays.asList(field1, field2, CollectionRetriever.RELEVANCE_FIELD),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2"), new DecimalValue(new BigDecimal(1f))),
                pair(id3, new ReferenceValue(id3), new StringValue("object 3"), new DecimalValue(new BigDecimal(1f))),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1"), new DecimalValue(new BigDecimal(.3f)))
                );
        assertThat(result, new IdentifiableObjectCollectionMatcher(expected));
    }

    @Test
    @Ignore // Порциональные запросы теперь не нужны
    public void testPartialFetch() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id1");
        when(doc1.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        when(doc2.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(.9f);
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id3");
        when(doc3.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(.5f);
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
        Id id5 = mock(Id.class);
        when(idService.createId("id5")).thenReturn(id5);

        FieldConfig field1 = mock(FieldConfig.class);
        when(field1.getName()).thenReturn("Ref");
        FieldConfig field2 = mock(FieldConfig.class);
        when(field2.getName()).thenReturn("Str");
        IdentifiableObjectCollection sample = collection(Arrays.asList(field1, field2),
                pair(id4, new ReferenceValue(id4), new StringValue("object 4")),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2")),
                pair(id5, new ReferenceValue(id5), new StringValue("object 5")),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1"))
                );
        when(collectionsService.findCollectionByQuery(anyString(), anyList(), eq(0), eq(20))).thenReturn(sample);

        IdentifiableObjectCollection result = retriever.queryCollection(docList, 20);

        IdentifiableObjectCollection expected = collection(Arrays.asList(field1, field2, CollectionRetriever.RELEVANCE_FIELD),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1"), new DecimalValue(new BigDecimal(1f))),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2"), new DecimalValue(new BigDecimal(.9f)))
                );
        assertThat(result, new IdentifiableObjectCollectionMatcher(expected));
    }

    @Test
    @Ignore // Повторный запрос теперь не требуется
    public void testRepeatedFetch() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id5");
        when(doc1.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id6");
        when(doc2.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(.9f);
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        when(doc3.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(.5f);
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
        Id id5 = mock(Id.class);
        when(idService.createId("id5")).thenReturn(id5);
        Id id6 = mock(Id.class);
        when(idService.createId("id6")).thenReturn(id6);
        Id id7 = mock(Id.class);
        when(idService.createId("id7")).thenReturn(id7);
        Id id8 = mock(Id.class);
        when(idService.createId("id8")).thenReturn(id8);

        FieldConfig field1 = mock(FieldConfig.class);
        when(field1.getName()).thenReturn("Id");
        FieldConfig field2 = mock(FieldConfig.class);
        when(field2.getName()).thenReturn("Id");
        IdentifiableObjectCollection sample1 = collection(Arrays.asList(field1, field2),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1")),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2")),
                pair(id3, new ReferenceValue(id3), new StringValue("object 3")),
                pair(id4, new ReferenceValue(id4), new StringValue("object 4")),
                pair(id5, new ReferenceValue(id5), new StringValue("object 5"))
                );
        IdentifiableObjectCollection sample2 = collection(Arrays.asList(field1, field2),
                pair(id6, new ReferenceValue(id6), new StringValue("object 6")),
                pair(id7, new ReferenceValue(id7), new StringValue("object 7")),
                pair(id8, new ReferenceValue(id8), new StringValue("object 8"))
                );
        when(collectionsService.findCollectionByQuery(anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(sample1, sample2);

        IdentifiableObjectCollection result = retriever.queryCollection(docList, 5);

        IdentifiableObjectCollection expected = collection(Arrays.asList(field1, field2, CollectionRetriever.RELEVANCE_FIELD),
                pair(id5, new ReferenceValue(id5), new StringValue("object 5"), new DecimalValue(new BigDecimal(1f))),
                pair(id6, new ReferenceValue(id6), new StringValue("object 6"), new DecimalValue(new BigDecimal(.9f))),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2"), new DecimalValue(new BigDecimal(.5f)))
                );
        assertThat(result, new IdentifiableObjectCollectionMatcher(expected));
    }

    @Test
    public void testParametrizedQuery() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id1");
        when(doc1.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(.3f);
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        when(doc2.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id3");
        when(doc3.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(doc2, doc3, doc1));    // mixed

        Id id1 = mock(Id.class);
        when(idService.createId("id1")).thenReturn(id1);
        Id id2 = mock(Id.class);
        when(idService.createId("id2")).thenReturn(id2);
        Id id3 = mock(Id.class);
        when(idService.createId("id3")).thenReturn(id3);

        FieldConfig field1 = mock(FieldConfig.class);
        when(field1.getName()).thenReturn("Id");
        FieldConfig field2 = mock(FieldConfig.class);
        when(field2.getName()).thenReturn("Id");
        IdentifiableObjectCollection sample = collection(Arrays.asList(field1, field2),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1")),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2")),
                pair(id3, new ReferenceValue(id3), new StringValue("object 3"))
                );
        when(collectionsService.findCollectionByQuery(anyString(), anyList(), eq(0), eq(20)))
                .thenReturn(sample);

        IdentifiableObjectCollection result = parametrizedRetriever.queryCollection(docList, 20);

        IdentifiableObjectCollection expected = collection(Arrays.asList(field1, field2, CollectionRetriever.RELEVANCE_FIELD),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2"), new DecimalValue(new BigDecimal(1f))),
                pair(id3, new ReferenceValue(id3), new StringValue("object 3"), new DecimalValue(new BigDecimal(1f))),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1"), new DecimalValue(new BigDecimal(.3f)))
                );
        assertThat(result, new IdentifiableObjectCollectionMatcher(expected));
    }

    private static Pair<Id, List<Value<?>>> pair(Id id, Value<?>... values) {
        return new Pair<Id, List<Value<?>>>(id, Arrays.asList(values));
    }

    @SafeVarargs
    private static IdentifiableObjectCollection collection(List<FieldConfig> fields, Pair<Id, List<Value<?>>>... objects) {
        GenericIdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
        collection.setFieldsConfiguration(fields);
        for (int i = 0; i < objects.length; ++i) {
            Pair<Id, List<Value<?>>> obj = objects[i];
            collection.setId(i, obj.getFirst());
            for (int j = 0; j < fields.size(); ++j) {
                Value<?> value = obj.getSecond().get(j);
                collection.set(j, i, value);
            }
        }
        return collection;
    }

    private static class IdentifiableObjectCollectionMatcher extends BaseMatcher<IdentifiableObjectCollection> {

        private IdentifiableObjectCollection expected;

        public IdentifiableObjectCollectionMatcher(IdentifiableObjectCollection expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object item) {
            IdentifiableObjectCollection actual = (IdentifiableObjectCollection) item;
            if (!expected.getFieldsConfiguration().equals(actual.getFieldsConfiguration())
                    || expected.size() != actual.size()) {
                return false;
            }
            for (int i = 0; i < expected.size(); ++i) {
                Id id = expected.getId(i);
                boolean found = false;
                for (int j = 0; j < expected.size(); j++) {
                    if (id.equals(actual.getId(j))) {
                        for (int k = 0; k < expected.getFieldsConfiguration().size(); k++) {
                            Value<?> valueExpected = expected.get(k, i);
                            Value<?> valueActual = actual.get(k, j);
                            if (valueExpected == null ? valueActual != null : !valueExpected.equals(valueActual)) {
                                return false;
                            }
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
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

    @Test
    public void testQueryModifier(){
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id1");
        when(doc1.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(.3f);
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        when(doc2.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id3");
        when(doc3.getFieldValue(SolrUtils.SCORE_FIELD)).thenReturn(1f);
        SolrDocumentList docList = new SolrDocumentList();
        docList.addAll(Arrays.asList(doc2, doc3, doc1));    // mixed

        Id id1 = mock(Id.class);
        when(idService.createId("id1")).thenReturn(id1);
        Id id2 = mock(Id.class);
        when(idService.createId("id2")).thenReturn(id2);
        Id id3 = mock(Id.class);
        when(idService.createId("id3")).thenReturn(id3);

        FieldConfig field1 = mock(FieldConfig.class);
        when(field1.getName()).thenReturn("Id");
        FieldConfig field2 = mock(FieldConfig.class);
        when(field2.getName()).thenReturn("Id");
        IdentifiableObjectCollection sample = collection(Arrays.asList(field1, field2),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1")),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2")),
                pair(id3, new ReferenceValue(id3), new StringValue("object 3"))
        );
        when(collectionsService.findCollectionByQuery(anyString(), anyList(), eq(0), eq(20)))
                .thenReturn(sample);

        List<Value> queryParams = new ArrayList<>();
        queryParams.add(new ReferenceValue(id2));
        queryParams.add(new ReferenceValue(id3));
        queryParams.add(new ReferenceValue(id1));

        ReflectionTestUtils.setField(retriever, "sqlQuery", "select id from type");
        retriever.queryCollection(docList, 20);
        verify(collectionsService).findCollectionByQuery(
                eq("select * from (select id from type) orig where (id={0} or id={1} or id={2})"),
                eq(queryParams),
                eq(0),
                eq(20));

        StringValue param = new StringValue("field_value");
        ReflectionTestUtils.setField(retriever, "sqlQuery", "select id from type where field = {0}");
        ReflectionTestUtils.setField(retriever, "sqlParameters", Collections.singletonList(param));
        retriever.queryCollection(docList, 20);
        queryParams.add(0, param);
        verify(collectionsService).findCollectionByQuery(
                eq("select * from (select id from type where field = {0}) orig where (id={1} or id={2} or id={3})"),
                eq(queryParams),
                eq(0),
                eq(20));
    }
}
