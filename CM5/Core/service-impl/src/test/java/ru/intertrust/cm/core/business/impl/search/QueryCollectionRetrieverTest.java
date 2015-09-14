package ru.intertrust.cm.core.business.impl.search;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.util.SpringApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class QueryCollectionRetrieverTest {

    @Mock private CollectionsService collectionsService;
    @Mock private IdService idService;

    @InjectMocks private QueryCollectionRetriever retriever = new QueryCollectionRetriever("Test SQL");

    @Mock private Value<?> param1;
    @Mock private Value<?> param2;
    private List<? extends Value<?>> parameters = Arrays.asList(param1, param2);
    @InjectMocks private QueryCollectionRetriever parametrizedRetriever =
            new QueryCollectionRetriever("Test SQL", parameters);

    static {
        ApplicationContext appCtx = mock(ApplicationContext.class);
        when(appCtx.getAutowireCapableBeanFactory()).thenAnswer(RETURNS_MOCKS);
        new SpringApplicationContext().setApplicationContext(appCtx);
    }

    @Test
    public void testFullFetch() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id1");
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id3");
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
        when(collectionsService.findCollectionByQuery("Test SQL", 0, 20)).thenReturn(sample);

        IdentifiableObjectCollection result = retriever.queryCollection(docList, 20);

        assertThat(result, new IdentifiableObjectCollectionMatcher(sample));
    }

    @Test
    public void testPartialFetch() {
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
        Id id5 = mock(Id.class);
        when(idService.createId("id5")).thenReturn(id5);

        FieldConfig field1 = mock(FieldConfig.class);
        when(field1.getName()).thenReturn("Id");
        FieldConfig field2 = mock(FieldConfig.class);
        when(field2.getName()).thenReturn("Id");
        IdentifiableObjectCollection sample = collection(Arrays.asList(field1, field2),
                pair(id4, new ReferenceValue(id4), new StringValue("object 4")),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2")),
                pair(id5, new ReferenceValue(id5), new StringValue("object 5")),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1"))
                );
        when(collectionsService.findCollectionByQuery("Test SQL", 0, 20)).thenReturn(sample);

        IdentifiableObjectCollection result = retriever.queryCollection(docList, 20);

        IdentifiableObjectCollection expected = collection(Arrays.asList(field1, field2),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2")),
                pair(id1, new ReferenceValue(id1), new StringValue("object 1"))
                );
        assertThat(result, new IdentifiableObjectCollectionMatcher(expected));
    }

    @Test
    public void testRepeatedFetch() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id5");
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id6");
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
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
        when(collectionsService.findCollectionByQuery(eq("Test SQL"), anyInt(), anyInt()))
                .thenReturn(sample1, sample2);

        IdentifiableObjectCollection result = retriever.queryCollection(docList, 5);

        IdentifiableObjectCollection expected = collection(Arrays.asList(field1, field2),
                pair(id2, new ReferenceValue(id2), new StringValue("object 2")),
                pair(id5, new ReferenceValue(id5), new StringValue("object 5")),
                pair(id6, new ReferenceValue(id6), new StringValue("object 6"))
                );
        assertThat(result, new IdentifiableObjectCollectionMatcher(expected));
    }

    @Test
    public void testParametrizedQuery() {
        SolrDocument doc1 = mock(SolrDocument.class);
        when(doc1.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id1");
        SolrDocument doc2 = mock(SolrDocument.class);
        when(doc2.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id2");
        SolrDocument doc3 = mock(SolrDocument.class);
        when(doc3.getFieldValue(SolrFields.MAIN_OBJECT_ID)).thenReturn("id3");
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
        when(collectionsService.findCollectionByQuery("Test SQL", parameters, 0, 20))
                .thenReturn(sample);

        IdentifiableObjectCollection result = parametrizedRetriever.queryCollection(docList, 20);

        assertThat(result, new IdentifiableObjectCollectionMatcher(sample));
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
}
