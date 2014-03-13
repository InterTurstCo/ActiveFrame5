package ru.intertrust.cm.core.business.impl.search;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.ParentLinkConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;

public class DomainObjectIndexAgentTest {

    @Mock private SolrUpdateRequestQueue requestQueue;
    @Mock private SearchConfigHelper configHelper;
    @Mock private DoelEvaluator doelEvaluator;
    @Mock private AccessControlService accessControlService;
    @Mock private AttachmentContentDao attachmentContentDao;

    @InjectMocks
    DomainObjectIndexAgent testee = new DomainObjectIndexAgent();

    @Captor ArgumentCaptor<Collection<SolrInputDocument>> documents;// = ArgumentCaptor.forClass(Collection.class);

    @Before
    public void init() {
        initMocks(this);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSaveGenericDocument_RootObjectManyFields() {
        // Модель конфигурации области поиска
        IndexedFieldConfig stringField = mock(IndexedFieldConfig.class);
        when(stringField.getName()).thenReturn("StringField");
        IndexedFieldConfig ruStringField = mock(IndexedFieldConfig.class);
        when(ruStringField.getName()).thenReturn("RuStringField");
        IndexedFieldConfig ruEnGeTextField = mock(IndexedFieldConfig.class);
        when(ruEnGeTextField.getName()).thenReturn("RuEnGeTextField");
        IndexedFieldConfig longField = mock(IndexedFieldConfig.class);
        when(longField.getName()).thenReturn("LongField");
        IndexedFieldConfig dateField = mock(IndexedFieldConfig.class);
        when(dateField.getName()).thenReturn("DateField");
        IndexedFieldConfig referenceField = mock(IndexedFieldConfig.class);
        when(referenceField.getName()).thenReturn("ReferenceField");
        IndexedFieldConfig doelField = mock(IndexedFieldConfig.class);
        when(doelField.getName()).thenReturn("DoelField");
        when(doelField.getDoel()).thenReturn("doel^multiple.strings");
        TargetDomainObjectConfig objectConfig = mock(TargetDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn("TestType");
        when(objectConfig.getFields()).thenReturn(Arrays.asList(
                stringField, ruStringField, ruEnGeTextField, longField, dateField, referenceField, doelField));
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TestType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);

        when(configHelper.findEffectiveConfigs(Mockito.any(DomainObject.class)))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.getFieldType(same(stringField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.STRING));
        when(configHelper.getFieldType(same(ruStringField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.STRING));
        when(configHelper.getFieldType(same(ruEnGeTextField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.TEXT));
        when(configHelper.getFieldType(same(longField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.LONG));
        when(configHelper.getFieldType(same(dateField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.DATETIME));
        when(configHelper.getFieldType(same(referenceField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.REFERENCE));
        when(configHelper.getFieldType(same(doelField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.STRING, true));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getSupportedLanguages(eq("RuStringField"), anyString())).thenReturn(Arrays.asList("ru"));
        when(configHelper.getSupportedLanguages(eq("RuEnGeTextField"), anyString())).thenReturn(
                Arrays.asList("ru", "en", "ge"));
        when(accessControlService.createSystemAccessToken(anyString())).thenAnswer(RETURNS_MOCKS);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue("StringField")).thenReturn(new StringValue("Test string"));
        when(object.getValue("RuStringField")).thenReturn(new StringValue("Test russian string"));
        when(object.getValue("RuEnGeTextField")).thenReturn(new StringValue("Test multi-language string"));
        when(object.getValue("LongField")).thenReturn(new LongValue(47L));
        when(object.getValue("DateField")).thenReturn(new DateTimeValue(new Date(12345L)));
        Id linkId = idMock("RefId");
        when(object.getValue("ReferenceField")).thenReturn(new ReferenceValue(linkId));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("StringField", "LongField", "DateField");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel^multiple.strings")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList(
                        (Value) new StringValue("String 1"), new StringValue("String 2"), new StringValue("String 3")));

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock, modMock, modMock));

        // Проверка правильности сформированного запроса к Solr
        ArgumentCaptor<Collection> documents = ArgumentCaptor.forClass(Collection.class);
        verify(requestQueue).addDocuments(documents.capture());
        assertEquals(1, documents.getValue().size());
        SolrInputDocument doc = (SolrInputDocument) documents.getValue().iterator().next();
        assertThat(doc, hasEntry(equalTo("cm_id"), hasProperty("value", equalTo("TestId"))));
        assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("TestArea"))));
        assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("TestType"))));
        assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("TestId"))));
        assertThat(doc, hasEntry(equalTo("cm_t_stringfield"), hasProperty("value", equalTo("Test string"))));
        assertThat(doc, hasEntry(equalTo("cm_ru_rustringfield"),
                hasProperty("value", equalTo("Test russian string"))));
        assertThat(doc, hasEntry(equalTo("cm_ru_ruengetextfield"),
                hasProperty("value", equalTo("Test multi-language string"))));
        assertThat(doc, hasEntry(equalTo("cm_en_ruengetextfield"),
                hasProperty("value", equalTo("Test multi-language string"))));
        assertThat(doc, hasEntry(equalTo("cm_ge_ruengetextfield"),
                hasProperty("value", equalTo("Test multi-language string"))));
        assertThat(doc, hasEntry(equalTo("cm_l_longfield"), hasProperty("value", equalTo(47L))));
        assertThat(doc, hasEntry(equalTo("cm_dt_datefield"), hasProperty("value", equalTo(new Date(12345L)))));
        assertThat(doc, hasEntry(equalTo("cm_r_referencefield"), hasProperty("value", equalTo("RefId"))));
        assertThat(doc, hasEntry(equalTo("cm_ts_doelfield"), hasProperty("value",
                        containsInAnyOrder("String 1", "String 2", "String 3"))));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSaveGenericDocument_LinkedObject() {
        // Модель конфигурации области поиска
        IndexedFieldConfig stringField = mock(IndexedFieldConfig.class);
        when(stringField.getName()).thenReturn("TestField");
        LinkedDomainObjectConfig objectConfig = mock(LinkedDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn("TestType");
        when(objectConfig.getFields()).thenReturn(Arrays.asList(stringField));
        ParentLinkConfig parentLinkConfig = mock(ParentLinkConfig.class);
        when(parentLinkConfig.getDoel()).thenReturn("doel.parent.link");
        when(objectConfig.getParentLink()).thenReturn(parentLinkConfig);
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TargetType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);

        when(configHelper.findEffectiveConfigs(Mockito.any(DomainObject.class)))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.getFieldType(same(stringField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.STRING));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(accessControlService.createSystemAccessToken(anyString())).thenAnswer(RETURNS_MOCKS);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue("TestField")).thenReturn(new StringValue("Test string"));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("TestField");
        Id parentId = idMock("ParentId");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.parent.link")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList((Value) new ReferenceValue(parentId)));

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock));

        // Проверка правильности сформированного запроса к Solr
        ArgumentCaptor<Collection> documents = ArgumentCaptor.forClass(Collection.class);
        verify(requestQueue).addDocuments(documents.capture());
        assertEquals(1, documents.getValue().size());
        SolrInputDocument doc = (SolrInputDocument) documents.getValue().iterator().next();
        assertThat(doc, hasEntry(equalTo("cm_id"), hasProperty("value", equalTo("TestId"))));
        assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("TestArea"))));
        assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("TargetType"))));
        assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("ParentId"))));
        assertThat(doc, hasEntry(equalTo("cm_t_testfield"), hasProperty("value", equalTo("Test string"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSaveGenericDocument_LinkedObjectNoParent() {
        // Модель конфигурации области поиска
        IndexedFieldConfig stringField = mock(IndexedFieldConfig.class);
        when(stringField.getName()).thenReturn("TestField");
        LinkedDomainObjectConfig objectConfig = mock(LinkedDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn("TestType");
        when(objectConfig.getFields()).thenReturn(Arrays.asList(stringField));
        ParentLinkConfig parentLinkConfig = mock(ParentLinkConfig.class);
        when(parentLinkConfig.getDoel()).thenReturn("doel.parent.link");
        when(objectConfig.getParentLink()).thenReturn(parentLinkConfig);
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TargetType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);

        when(configHelper.findEffectiveConfigs(Mockito.any(DomainObject.class)))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.getFieldType(same(stringField), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.STRING));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(accessControlService.createSystemAccessToken(anyString())).thenAnswer(RETURNS_MOCKS);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue("TestField")).thenReturn(new StringValue("Test string"));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("TestField");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.parent.link")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList(new Value[0]));

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock));

        // Проверка правильности сформированного запроса к Solr
        verify(requestQueue, never()).addDocuments(anyCollection());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSaveGenericDocument_MultipleAreasDifferentConfigs() {
        // Модель конфигурации областей поиска
        IndexedFieldConfig field1 = mock(IndexedFieldConfig.class);
        when(field1.getName()).thenReturn("FieldA");
        TargetDomainObjectConfig config1 = mock(TargetDomainObjectConfig.class);
        when(config1.getType()).thenReturn("TestType");
        when(config1.getFields()).thenReturn(Arrays.asList(field1));
        SearchConfigHelper.SearchAreaDetailsConfig area1 = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(area1.getAreaName()).thenReturn("AreaA");
        when(area1.getTargetObjectType()).thenReturn("TestType");
        when(area1.getObjectConfig()).thenReturn(config1);

        IndexedFieldConfig field2 = mock(IndexedFieldConfig.class);
        when(field2.getName()).thenReturn("FieldB");
        LinkedDomainObjectConfig config2 = mock(LinkedDomainObjectConfig.class);
        when(config2.getType()).thenReturn("TestType");
        when(config2.getFields()).thenReturn(Arrays.asList(field2));
        ParentLinkConfig parent = mock(ParentLinkConfig.class);
        when(parent.getDoel()).thenReturn("doel.parent.link");
        when(config2.getParentLink()).thenReturn(parent);
        SearchConfigHelper.SearchAreaDetailsConfig area2 = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(area2.getAreaName()).thenReturn("AreaA");
        when(area2.getTargetObjectType()).thenReturn("ParentType");
        when(area2.getObjectConfig()).thenReturn(config2);

        IndexedFieldConfig field3a = mock(IndexedFieldConfig.class);
        when(field3a.getName()).thenReturn("FieldB");
        IndexedFieldConfig field3b = mock(IndexedFieldConfig.class);
        when(field3b.getName()).thenReturn("FieldC");
        TargetDomainObjectConfig config3 = mock(TargetDomainObjectConfig.class);
        when(config3.getType()).thenReturn("TestType");
        when(config3.getFields()).thenReturn(Arrays.asList(field3a, field3b));
        SearchConfigHelper.SearchAreaDetailsConfig area3 = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(area3.getAreaName()).thenReturn("AreaB");
        when(area3.getTargetObjectType()).thenReturn("TestType");
        when(area3.getObjectConfig()).thenReturn(config3);

        IndexedFieldConfig field4 = mock(IndexedFieldConfig.class);
        when(field4.getName()).thenReturn("FieldD");
        TargetDomainObjectConfig config4 = mock(TargetDomainObjectConfig.class);
        when(config4.getType()).thenReturn("TestType");
        when(config4.getFields()).thenReturn(Arrays.asList(field4));
        SearchConfigHelper.SearchAreaDetailsConfig area4 = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(area4.getAreaName()).thenReturn("AreaC");
        when(area4.getTargetObjectType()).thenReturn("TestType");
        when(area4.getObjectConfig()).thenReturn(config4);

        when(configHelper.findEffectiveConfigs(Mockito.any(DomainObject.class)))
                .thenReturn(Arrays.asList(area1, area2, area3, area4));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.getFieldType(Mockito.any(IndexedFieldConfig.class), anyString()))
                .thenReturn(new SearchConfigHelper.FieldDataType(FieldType.STRING));
        when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(accessControlService.createSystemAccessToken(anyString())).thenAnswer(RETURNS_MOCKS);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue("FieldA")).thenReturn(new StringValue("String A"));
        when(object.getValue("FieldB")).thenReturn(new StringValue("String B"));
        when(object.getValue("FieldC")).thenReturn(new StringValue("String C"));
        when(object.getValue("FieldD")).thenReturn(new StringValue("String D"));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("FieldA", "FieldB");
        Id parentId = idMock("ParentId");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.parent.link")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList((Value) new ReferenceValue(parentId)));

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock));

        // Проверка правильности сформированного запроса к Solr
        //ArgumentCaptor<Collection<SolrInputDocument>> documents;// = ArgumentCaptor.forClass(Collection.class);
        verify(requestQueue).addDocuments(documents.capture());

        assertEquals(4, documents.getValue().size());
        assertThat(documents.getValue(), containsInAnyOrder(
                hasEntry(equalTo("id"), hasProperty("value", equalTo("TestId:AreaA:TestType"))),
                hasEntry(equalTo("id"), hasProperty("value", equalTo("TestId:AreaA:ParentType"))),
                hasEntry(equalTo("id"), hasProperty("value", equalTo("TestId:AreaB:TestType"))),
                hasEntry(equalTo("id"), hasProperty("value", equalTo("TestId:AreaC:TestType")))));
        for (SolrInputDocument doc : (Collection<SolrInputDocument>)documents.getValue()) {
            assertThat(doc, hasEntry(equalTo("cm_id"), hasProperty("value", equalTo("TestId"))));
            String solrId = (String) doc.getFieldValue("id");
            if ("TestId:AreaA:TestType".equals(solrId)) {
                assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("AreaA"))));
                assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("TestType"))));
                assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("TestId"))));
                assertThat(doc, hasEntry(equalTo("cm_t_fielda"), hasProperty("value", equalTo("String A"))));
                assertThat(doc, not(hasKey("cm_t_fieldb")));
                assertThat(doc, not(hasKey("cm_t_fieldc")));
                assertThat(doc, not(hasKey("cm_t_fieldd")));
            } else if ("TestId:AreaA:ParentType".equals(solrId)) {
                assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("AreaA"))));
                assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("ParentType"))));
                assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("ParentId"))));
                assertThat(doc, not(hasKey("cm_t_fielda")));
                assertThat(doc, hasEntry(equalTo("cm_t_fieldb"), hasProperty("value", equalTo("String B"))));
                assertThat(doc, not(hasKey("cm_t_fieldc")));
                assertThat(doc, not(hasKey("cm_t_fieldd")));
            } else if ("TestId:AreaB:TestType".equals(solrId)) {
                assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("AreaB"))));
                assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("TestType"))));
                assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("TestId"))));
                assertThat(doc, not(hasKey("cm_t_fielda")));
                assertThat(doc, hasEntry(equalTo("cm_t_fieldb"), hasProperty("value", equalTo("String B"))));
                assertThat(doc, hasEntry(equalTo("cm_t_fieldc"), hasProperty("value", equalTo("String C"))));
                assertThat(doc, not(hasKey("cm_t_fieldd")));
            } else if ("TestId:AreaC:TestType".equals(solrId)) {
                assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("AreaC"))));
                assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("TestType"))));
                assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("TestId"))));
                assertThat(doc, not(hasKey("cm_t_fielda")));
                assertThat(doc, not(hasKey("cm_t_fieldb")));
                assertThat(doc, not(hasKey("cm_t_fieldc")));
                assertThat(doc, hasEntry(equalTo("cm_t_fieldd"), hasProperty("value", equalTo("String D"))));
            }
        }
    }

    @Test
    public void testSaveAttachment() throws Exception {
        // Модель конфигурации области поиска
        TargetDomainObjectConfig objectConfig = mock(TargetDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn("TargetType");
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TargetType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);

        when(configHelper.findEffectiveConfigs(Mockito.any(DomainObject.class)))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(true);
        when(configHelper.getAttachmentParentLinkName("TestType", "TargetType")).thenReturn("ParentLink");
        when(accessControlService.createSystemAccessToken(anyString())).thenAnswer(RETURNS_MOCKS);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue(AttachmentService.NAME)).thenReturn(new StringValue("Attachment name"));
        when(object.getValue(AttachmentService.DESCRIPTION)).thenReturn(new StringValue("Attachment description"));
        when(object.getValue(AttachmentService.CONTENT_LENGTH)).thenReturn(new LongValue(1500L));
        when(object.getModifiedDate()).thenReturn(new Date(55777L));
        Id parentId = idMock("ParentId");
        when(object.getReference("ParentLink")).thenReturn(parentId);
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn(AttachmentService.NAME);
        InputStream contentMock = mock(InputStream.class);
        when(attachmentContentDao.loadContent(Mockito.any(DomainObject.class))).thenReturn(contentMock);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock));

        // Проверка правильности сформированного запроса к Solr
        ArgumentCaptor<ContentStreamUpdateRequest> request = ArgumentCaptor.forClass(ContentStreamUpdateRequest.class);
        verify(requestQueue).addRequest(request.capture());
        assertTrue(request.getValue().getContentStreams().size() == 1);
        assertSame(contentMock, request.getValue().getContentStreams().iterator().next().getStream());
        Map<String, String> params = SolrParams.toMap(request.getValue().getParams().toNamedList());
        assertThat(params, hasEntry("literal.cm_id", "TestId"));
        assertThat(params, hasEntry("literal.cm_main", "ParentId"));
        assertThat(params, hasEntry("literal.cm_area", "TestArea"));
        assertThat(params, hasEntry("literal.cm_type", "TargetType"));
        assertThat(params, hasEntry("literal.cm_t_name", "Attachment name"));
        assertThat(params, hasEntry("literal.cm_t_description", "Attachment description"));
        assertThat(params, hasEntry("literal.cm_l_contentlength", "1500"));
        assertThat(params, hasEntry("uprefix", "cm_c_"));
        assertThat(params, hasEntry("fmap.content", "cm_content"));
    }

    private Id idMock(String stringRepresentation) {
        Id id = mock(Id.class);
        when(id.toStringRepresentation()).thenReturn(stringRepresentation);
        return id;
    }
}
