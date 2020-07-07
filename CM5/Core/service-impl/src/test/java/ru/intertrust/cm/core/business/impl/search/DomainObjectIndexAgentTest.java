package ru.intertrust.cm.core.business.impl.search;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.ScriptContext;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.ParentLinkConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.Subject;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

public class DomainObjectIndexAgentTest {

    @Mock private SolrUpdateRequestQueue requestQueue;
    @Mock private SearchConfigHelper configHelper;
    @Mock private DoelEvaluator doelEvaluator;
    @Mock private DomainObjectDao domainObjectDao;
    @Mock private ScriptService scriptService;

    @Mock private AccessControlService accessControlService;
    @Mock private AttachmentContentDao attachmentContentDao;

    @Mock private SolrServerWrapperMap solrServerWrapperMap;
    @Mock private SolrServerWrapper solrServerWrapper;

    @InjectMocks
    DomainObjectIndexAgent testee = new DomainObjectIndexAgent();

    @Captor ArgumentCaptor<Collection<SolrInputDocument>> documents;// = ArgumentCaptor.forClass(Collection.class);

    AccessToken mockToken = new AccessToken() {
        @Override
        public Subject getSubject() {
            return null;
        }

        @Override
        public boolean isDeferred() {
            return false;
        }

        @Override
        public AccessLimitationType getAccessLimitationType() {
            return AccessLimitationType.LIMITED;
        }
    };

    @Before
    public void init() {
        initMocks(this);
        when(solrServerWrapperMap.getRegularSolrServerWrapper()).thenReturn(solrServerWrapper);
        when(solrServerWrapper.getQueue()).thenReturn(requestQueue);
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
        IndexedFieldConfig decimalField = mock(IndexedFieldConfig.class);
        when(decimalField.getName()).thenReturn("DecimalField");
        IndexedFieldConfig dateField = mock(IndexedFieldConfig.class);
        when(dateField.getName()).thenReturn("DateField");
        IndexedFieldConfig referenceField = mock(IndexedFieldConfig.class);
        when(referenceField.getName()).thenReturn("ReferenceField");
        IndexedFieldConfig doelField = mock(IndexedFieldConfig.class);
        when(doelField.getName()).thenReturn("DoelField");
        when(doelField.getDoel()).thenReturn("doel^multiple.strings");
        IndexedFieldConfig customField = mock(IndexedFieldConfig.class);
        when(customField.getName()).thenReturn("CustomField");
        when(customField.getSolrPrefix()).thenReturn("custom");
        IndexedFieldConfig scriptField = mock(IndexedFieldConfig.class);
        when(scriptField.getName()).thenReturn("ScriptField");
        when(scriptField.getScript()).thenReturn("evaluate");
        // Поля поисковой области, вычисляемые скриптами
        /*IndexedFieldConfig scriptStringField = mock(IndexedFieldConfig.class);
        when(scriptStringField.getName()).thenReturn("ScriptStringField");
        when(scriptStringField.getScript()).thenReturn("return String");
        IndexedFieldConfig scriptLongField = mock(IndexedFieldConfig.class);
        when(scriptLongField.getName()).thenReturn("ScriptLongField");
        when(scriptLongField.getScript()).thenReturn("return Long");
        IndexedFieldConfig scriptDateField = mock(IndexedFieldConfig.class);
        when(scriptDateField.getName()).thenReturn("ScriptDateField");
        when(scriptDateField.getScript()).thenReturn("return Date");
        IndexedFieldConfig scriptBoolField = mock(IndexedFieldConfig.class);
        when(scriptBoolField.getName()).thenReturn("ScriptBoolField");
        when(scriptBoolField.getScript()).thenReturn("return Boolean");
        IndexedFieldConfig scriptFloatArrayField = mock(IndexedFieldConfig.class);
        when(scriptFloatArrayField.getName()).thenReturn("ScriptFloatArrayField");
        when(scriptFloatArrayField.getScript()).thenReturn("return Array of Floats");*/

        TargetDomainObjectConfig objectConfig = mock(TargetDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn("TestType");
        when(objectConfig.getFields()).thenReturn(Arrays.asList(
                stringField, ruStringField, ruEnGeTextField,
                longField, decimalField, dateField, referenceField, doelField, customField,
                scriptField));
                //scriptStringField, scriptLongField, scriptDateField, scriptBoolField, scriptFloatArrayField));
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TestType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);
        when(areaConfig.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[] { objectConfig });

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType("TestType", "TestType")).thenReturn(true);
        when(configHelper.getFieldTypes(same(stringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList(""), false, false)));
        when(configHelper.getFieldTypes(same(ruStringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList("ru"), false, false)));
        when(configHelper.getFieldTypes(same(ruEnGeTextField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList("ru", "en", "ge"), false, false)));
        when(configHelper.getFieldTypes(same(longField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG)));
        when(configHelper.getFieldTypes(same(decimalField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.DOUBLE)));
        when(configHelper.getFieldTypes(same(dateField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.DATE)));
        when(configHelper.getFieldTypes(same(referenceField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.REF)));
        when(configHelper.getFieldTypes(same(doelField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList(""), true, false)));
        when(configHelper.getFieldTypes(same(scriptField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList(""), false, false)));
        when(configHelper.getFieldTypes(same(customField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new CustomSearchFieldType("custom_")));
        /*when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getSupportedLanguages(eq("RuStringField"), anyString())).thenReturn(Arrays.asList("ru"));
        when(configHelper.getSupportedLanguages(eq("RuEnGeTextField"), anyString())).thenReturn(
                Arrays.asList("ru", "en", "ge"));*/
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList(""));

        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue("StringField")).thenReturn(new StringValue("Test string"));
        when(object.getValue("RuStringField")).thenReturn(new StringValue("Test russian string"));
        when(object.getValue("RuEnGeTextField")).thenReturn(new StringValue("Test multi-language string"));
        when(object.getValue("LongField")).thenReturn(new LongValue(47L));
        when(object.getValue("DecimalField")).thenReturn(new DecimalValue(new BigDecimal("11.76")));
        when(object.getValue("DateField")).thenReturn(new DateTimeValue(new Date(12345L)));
        Id linkId = idMock("RefId");
        when(object.getValue("ReferenceField")).thenReturn(new ReferenceValue(linkId));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("StringField", "LongField", "DateField");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel^multiple.strings")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList(
                        (Value) new StringValue("String 1"), new StringValue("String 2"), new StringValue("String 3")));
        when(object.getValue("CustomField")).thenReturn(new StringValue("Custom indexed string"));

        /*@SuppressWarnings("deprecation")
        Date calculatedDate = new Date(Date.parse("Thu, 9 Jul 2015 10:15:25 GMT+0300"));
        when(scriptService.eval(eq("return String"), any(ScriptContext.class))).thenReturn("Calculated");
        when(scriptService.eval(eq("return Long"), any(ScriptContext.class))).thenReturn(555L);
        when(scriptService.eval(eq("return Date"), any(ScriptContext.class))).thenReturn(calculatedDate);
        when(scriptService.eval(eq("return Boolean"), any(ScriptContext.class))).thenReturn(true);
        when(scriptService.eval(eq("return Array of Floats"), any(ScriptContext.class)))
                .thenReturn(new Float[] { 2.81f, 3.14f } );*/
        when(scriptService.eval(eq("evaluate"), any(ScriptContext.class))).thenReturn("Calculated");

        when(domainObjectDao.find(id, mockToken)).thenReturn(object);

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
        assertThat(doc, hasEntry(equalTo("cm_ru_rustringfield"), hasProperty("value", equalTo("Test russian string"))));
        assertThat(doc, hasEntry(equalTo("cm_ru_ruengetextfield"),
                hasProperty("value", equalTo("Test multi-language string"))));
        assertThat(doc, hasEntry(equalTo("cm_en_ruengetextfield"),
                hasProperty("value", equalTo("Test multi-language string"))));
        assertThat(doc, hasEntry(equalTo("cm_ge_ruengetextfield"),
                hasProperty("value", equalTo("Test multi-language string"))));
        assertThat(doc, hasEntry(equalTo("cm_l_longfield"), hasProperty("value", equalTo(47L))));
        assertThat(doc, hasEntry(equalTo("cm_d_decimalfield"), hasProperty("value", equalTo(new BigDecimal("11.76")))));
        assertThat(doc, hasEntry(equalTo("cm_dt_datefield"), hasProperty("value", equalTo(new Date(12345L)))));
        assertThat(doc, hasEntry(equalTo("cm_r_referencefield"), hasProperty("value", equalTo("RefId"))));
        assertThat(doc, hasEntry(equalTo("cm_ts_doelfield"), hasProperty("value",
                        containsInAnyOrder("String 1", "String 2", "String 3"))));
        assertThat(doc, hasEntry(equalTo("custom_customfield"), hasProperty("value", equalTo("Custom indexed string"))));
        assertThat(doc, hasEntry(equalTo("cm_t_scriptfield"), hasProperty("value", equalTo("Calculated"))));
        /*assertThat(doc, hasEntry(equalTo("cm_t_scriptstringfield"), hasProperty("value", equalTo("Calculated"))));
        assertThat(doc, hasEntry(equalTo("cm_l_scriptlongfield"), hasProperty("value", equalTo(555L))));
        assertThat(doc, hasEntry(equalTo("cm_dt_scriptdatefield"), hasProperty("value", equalTo(calculatedDate))));
        assertThat(doc, hasEntry(equalTo("cm_b_scriptboolfield"), hasProperty("value", equalTo(true))));
        assertThat(doc, hasEntry(equalTo("cm_ds_scriptfloatarrayfield"), hasProperty("value")));   //*****/

        verify(requestQueue, never()).addRequest(any(AbstractUpdateRequest.class));
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
        TargetDomainObjectConfig parentObjectConfig = mock(TargetDomainObjectConfig.class);
        when(parentObjectConfig.getType()).thenReturn("TargetType");
        ParentLinkConfig parentLinkConfig = mock(ParentLinkConfig.class);
        when(parentLinkConfig.getDoel()).thenReturn("doel.parent.link");
        when(objectConfig.getParentLink()).thenReturn(parentLinkConfig);
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TargetType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);
        when(areaConfig.getObjectConfigChain()).thenReturn(
                new IndexedDomainObjectConfig[] { objectConfig, parentObjectConfig });

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType(anyString(), anyString())).thenReturn(true);
        when(configHelper.getFieldTypes(same(stringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList(""), false, false)));
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue("TestField")).thenReturn(new StringValue("Test string"));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("TestField");
        Id parentId = idMock("ParentId");
        DomainObject parent = mock(DomainObject.class);
        when(parent.getTypeName()).thenReturn("TargetType");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.parent.link")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList((Value) new ReferenceValue(parentId)));
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parent);

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

        verify(requestQueue, never()).addRequest(any(AbstractUpdateRequest.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSaveGenericDocument_IndirectLinkedObject() {
        // Модель конфигурации области поиска
        IndexedFieldConfig stringField = mock(IndexedFieldConfig.class);
        when(stringField.getName()).thenReturn("TestField");
        LinkedDomainObjectConfig objectConfig = mock(LinkedDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn("TestType");
        when(objectConfig.getFields()).thenReturn(Arrays.asList(stringField));
        LinkedDomainObjectConfig intermediateConfig = mock(LinkedDomainObjectConfig.class);
        when(intermediateConfig.getType()).thenReturn("IntermediateType");
        ParentLinkConfig intermedLinkConfig = mock(ParentLinkConfig.class);
        when(intermedLinkConfig.getDoel()).thenReturn("doel.intermediate.link");
        when(objectConfig.getParentLink()).thenReturn(intermedLinkConfig);
        ParentLinkConfig parentLinkConfig = mock(ParentLinkConfig.class);
        when(parentLinkConfig.getDoel()).thenReturn("doel.parent.link");
        when(intermediateConfig.getParentLink()).thenReturn(parentLinkConfig);
        TargetDomainObjectConfig parentObjectConfig = mock(TargetDomainObjectConfig.class);
        when(parentObjectConfig.getType()).thenReturn("TargetType");
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TargetType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);
        when(areaConfig.getObjectConfigChain()).thenReturn(
                new IndexedDomainObjectConfig[] { objectConfig, intermediateConfig, parentObjectConfig });

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType(anyString(), anyString())).thenReturn(true);
        when(configHelper.getFieldTypes(same(stringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList(""), false, false)));
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue("TestField")).thenReturn(new StringValue("Test string"));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("TestField");
        Id intermediateId = idMock("IntermediateId");
        DomainObject intermediate = mock(DomainObject.class);
        when(intermediate.getTypeName()).thenReturn("IntermediateType");
        Id parentId = idMock("ParentId");
        DomainObject parent = mock(DomainObject.class);
        when(parent.getTypeName()).thenReturn("TargetType");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.intermediate.link")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList((Value) new ReferenceValue(intermediateId)));
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.parent.link")), same(intermediateId),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList((Value) new ReferenceValue(parentId)));
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(intermediateId, mockToken)).thenReturn(intermediate);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parent);

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

        verify(requestQueue, never()).addRequest(any(AbstractUpdateRequest.class));
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
        TargetDomainObjectConfig parentObjectConfig = mock(TargetDomainObjectConfig.class);
        when(parentObjectConfig.getType()).thenReturn("TargetType");
        ParentLinkConfig parentLinkConfig = mock(ParentLinkConfig.class);
        when(parentLinkConfig.getDoel()).thenReturn("doel.parent.link");
        when(objectConfig.getParentLink()).thenReturn(parentLinkConfig);
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TargetType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);
        when(areaConfig.getObjectConfigChain()).thenReturn(
                new IndexedDomainObjectConfig[] { objectConfig, parentObjectConfig });

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType(anyString(), anyString())).thenReturn(true);
        when(configHelper.getFieldTypes(same(stringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList(""), false, false)));
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

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
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock));

        // Проверка правильности сформированного запроса к Solr
        verify(requestQueue, never()).addDocuments(anyCollection());
        ArgumentCaptor<UpdateRequest> request = ArgumentCaptor.forClass(UpdateRequest.class);
        verify(requestQueue).addRequest(request.capture());
        assertThat(request.getValue().getDeleteById(), containsInAnyOrder("TestId:TestArea:TargetType"));
    }

    /**
     * Проверяет ситуацию, описанную в CMFIVE-1052
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testSaveGenericDocument_LinkedFitsOneAreaByParentType() {
        // Модель конфигурации областей поиска
        IndexedFieldConfig field = mock(IndexedFieldConfig.class);
        when(field.getName()).thenReturn("TestField");
        ParentLinkConfig parentLink = mock(ParentLinkConfig.class);
        when(parentLink.getDoel()).thenReturn("doel.parent.link");

        TargetDomainObjectConfig parentFit = mock(TargetDomainObjectConfig.class);
        when(parentFit.getType()).thenReturn("ParentFit");
        LinkedDomainObjectConfig configFit = mock(LinkedDomainObjectConfig.class);
        when(configFit.getType()).thenReturn("TestType");
        when(configFit.getFields()).thenReturn(Arrays.asList(field));
        when(configFit.getParentLink()).thenReturn(parentLink);
        SearchConfigHelper.SearchAreaDetailsConfig areaFit = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaFit.getAreaName()).thenReturn("AreaFit");
        when(areaFit.getTargetObjectType()).thenReturn("ParentFit");
        when(areaFit.getObjectConfig()).thenReturn(configFit);
        when(areaFit.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[] { configFit, parentFit });

        TargetDomainObjectConfig parentNotFit = mock(TargetDomainObjectConfig.class);
        when(parentNotFit.getType()).thenReturn("ParentNotFit");
        LinkedDomainObjectConfig configNotFit = mock(LinkedDomainObjectConfig.class);
        when(configNotFit.getType()).thenReturn("TestType");
        when(configNotFit.getFields()).thenReturn(Arrays.asList(field));
        when(configNotFit.getParentLink()).thenReturn(parentLink);
        SearchConfigHelper.SearchAreaDetailsConfig areaNotFit = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaNotFit.getAreaName()).thenReturn("AreaNotFit");
        when(areaNotFit.getTargetObjectType()).thenReturn("ParentNotFit");
        when(areaNotFit.getObjectConfig()).thenReturn(configNotFit);
        when(areaNotFit.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[]
                { configNotFit, parentNotFit });

        when(configHelper.findEffectiveConfigs(anyString())).thenReturn(Arrays.asList(areaFit, areaNotFit));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType("TestType", "TestType")).thenReturn(true);
        when(configHelper.isSuitableType("ParentFit", "ParentType")).thenReturn(true);
        when(configHelper.isSuitableType("ParentNotFit", "ParentType")).thenReturn(false);
        when(configHelper.getFieldTypes(same(field), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG)));
        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue("TestField")).thenReturn(new LongValue(100500));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("TestField");
        DomainObject parentObject = mock(DomainObject.class);
        when(parentObject.getTypeName()).thenReturn("ParentType");
        Id parentId = idMock("ParentId");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.parent.link")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList((Value) new ReferenceValue(parentId)));
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parentObject);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock));

        // Проверка правильности сформированного запроса к Solr
        ArgumentCaptor<Collection> documents = ArgumentCaptor.forClass(Collection.class);
        verify(requestQueue).addDocuments(documents.capture());
        assertEquals(1, documents.getValue().size());
        SolrInputDocument doc = (SolrInputDocument) documents.getValue().iterator().next();
        assertThat(doc, hasEntry(equalTo("cm_id"), hasProperty("value", equalTo("TestId"))));
        assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("AreaFit"))));
        assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("ParentFit"))));
        assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("ParentId"))));
        assertThat(doc, hasEntry(equalTo("cm_l_testfield"), hasProperty("value", equalTo(100500L))));
        ArgumentCaptor<UpdateRequest> request = ArgumentCaptor.forClass(UpdateRequest.class);
        verify(requestQueue).addRequest(request.capture());
        assertThat(request.getValue().getDeleteById(), containsInAnyOrder("TestId:AreaNotFit:ParentNotFit"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        when(area1.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[] { config1 });

        IndexedFieldConfig field2 = mock(IndexedFieldConfig.class);
        when(field2.getName()).thenReturn("FieldB");
        LinkedDomainObjectConfig config2 = mock(LinkedDomainObjectConfig.class);
        when(config2.getType()).thenReturn("TestType");
        when(config2.getFields()).thenReturn(Arrays.asList(field2));
        TargetDomainObjectConfig parentObjectConfig = mock(TargetDomainObjectConfig.class);
        when(parentObjectConfig.getType()).thenReturn("ParentType");
        ParentLinkConfig parent = mock(ParentLinkConfig.class);
        when(parent.getDoel()).thenReturn("doel.parent.link");
        when(config2.getParentLink()).thenReturn(parent);
        SearchConfigHelper.SearchAreaDetailsConfig area2 = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(area2.getAreaName()).thenReturn("AreaA");
        when(area2.getTargetObjectType()).thenReturn("ParentType");
        when(area2.getObjectConfig()).thenReturn(config2);
        when(area2.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[] { config2, parentObjectConfig });

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
        when(area3.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[] { config3 });

        IndexedFieldConfig field4 = mock(IndexedFieldConfig.class);
        when(field4.getName()).thenReturn("FieldD");
        TargetDomainObjectConfig config4 = mock(TargetDomainObjectConfig.class);
        when(config4.getType()).thenReturn("TestType");
        when(config4.getFields()).thenReturn(Arrays.asList(field4));
        SearchConfigHelper.SearchAreaDetailsConfig area4 = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(area4.getAreaName()).thenReturn("AreaC");
        when(area4.getTargetObjectType()).thenReturn("TestType");
        when(area4.getObjectConfig()).thenReturn(config4);
        when(area4.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[] { config4 });

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(area1, area2, area3, area4));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType(anyString(), anyString())).thenReturn(true);
        when(configHelper.getFieldTypes(Mockito.any(IndexedFieldConfig.class), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList(""), false, false)));
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

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
        DomainObject parentObject = mock(DomainObject.class);
        when(parentObject.getTypeName()).thenReturn("ParentType");
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.parent.link")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList((Value) new ReferenceValue(parentId)));
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parentObject);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock));

        // Проверка правильности сформированного запроса к Solr
        //ArgumentCaptor<Collection<SolrInputDocument>> documents;// = ArgumentCaptor.forClass(Collection.class);
        verify(requestQueue).addDocuments(documents.capture());
        verify(requestQueue, never()).addRequest(any(AbstractUpdateRequest.class));

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSaveGenericDocument_TypeInheritance() {
        // Модель конфигурации области поиска
        IndexedFieldConfig childTypeField = mock(IndexedFieldConfig.class);
        when(childTypeField.getName()).thenReturn("ChildTypeField");
        TargetDomainObjectConfig objectConfig = mock(TargetDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn("ParentType");
        when(objectConfig.getFields()).thenReturn(Arrays.asList(childTypeField));
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("ParentType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);
        when(areaConfig.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[] { objectConfig });

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType("ParentType", "ChildType")).thenReturn(true);
        when(configHelper.getFieldTypes(same(childTypeField), eq("ChildType"))).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList(""), false, false)));
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList(""));

        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("ChildType");
        when(object.getValue("ChildTypeField")).thenReturn(new StringValue("Test string"));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("ChildTypeField");

        when(domainObjectDao.find(id, mockToken)).thenReturn(object);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Arrays.asList(modMock));

        // Проверка правильности сформированного запроса к Solr
        ArgumentCaptor<Collection> documents = ArgumentCaptor.forClass(Collection.class);
        verify(requestQueue).addDocuments(documents.capture());
        assertEquals(1, documents.getValue().size());
        SolrInputDocument doc = (SolrInputDocument) documents.getValue().iterator().next();
        assertThat(doc, hasEntry(equalTo("cm_id"), hasProperty("value", equalTo("TestId"))));
        assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("TestArea"))));
        assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("ParentType"))));
        assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("TestId"))));
        assertThat(doc, hasEntry(equalTo("cm_t_childtypefield"), hasProperty("value", equalTo("Test string"))));

        verify(requestQueue, never()).addRequest(any(AbstractUpdateRequest.class));
    }

    /*@Test
    public void testSaveGenericDocument_HavingLinked() {
        // Модель конфигурации области поиска
        IndexedFieldConfig stringField = mock(IndexedFieldConfig.class);
        when(stringField.getName()).thenReturn("StringField");

        IndexedFieldConfig longField = mock(IndexedFieldConfig.class);
        when(longField.getName()).thenReturn("LongField");

        LinkedDomainObjectConfig childConfig = mock(LinkedDomainObjectConfig.class);
        when(childConfig.getType()).thenReturn("ChildType");
        when(childConfig.getFields()).thenReturn(Arrays.asList(longField));
        ParentLinkConfig linkConfig = mock(ParentLinkConfig.class);
        when(linkConfig.getDoel()).thenReturn("doel.get.parent");
        when(childConfig.getParentLink()).thenReturn(linkConfig);
        TargetDomainObjectConfig parentConfig = mock(TargetDomainObjectConfig.class);
        when(parentConfig.getType()).thenReturn("ParentType");
        when(parentConfig.getFields()).thenReturn(Arrays.asList(stringField));
        when(parentConfig.getLinkedObjects()).thenReturn(Arrays.asList(childConfig));
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig =
                new SearchConfigHelper.SearchAreaDetailsConfig(Arrays.<IndexedDomainObjectConfig>asList(parentConfig),
                        "TestArea", "ParentType");

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType("ParentType", "TestType")).thenReturn(true);
        when(configHelper.getFieldType(same(stringField), anyString())).thenReturn(SearchFieldType.TEXT);
        when(configHelper.getFieldType(same(longField), anyString())).thenReturn(SearchFieldType.LONG);
        //when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));

        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Модель сохраняемого объекта и его изменений
        DomainObject parent = mock(DomainObject.class);
        Id parentId = idMock("ParentId");
        when(parent.getId()).thenReturn(parentId);
        when(parent.getTypeName()).thenReturn("ParentType");
        when(parent.getValue("StringField")).thenReturn(new StringValue("Test string"));
        when(object.getValue("LongField")).thenReturn(new LongValue(47L));
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn("StringField", "LongField", "DateField");

        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel^multiple.strings")), same(id),
                Mockito.any(AccessToken.class))).thenReturn(Arrays.asList(
                        (Value) new StringValue("String 1"), new StringValue("String 2"), new StringValue("String 3")));

        when(domainObjectDao.find(id, mockToken)).thenReturn(object);

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
        assertThat(doc, hasEntry(equalTo("cm_d_decimalfield"), hasProperty("value", equalTo(new BigDecimal("11.76")))));
        assertThat(doc, hasEntry(equalTo("cm_dt_datefield"), hasProperty("value", equalTo(new Date(12345L)))));
        assertThat(doc, hasEntry(equalTo("cm_r_referencefield"), hasProperty("value", equalTo("RefId"))));
        assertThat(doc, hasEntry(equalTo("cm_ts_doelfield"), hasProperty("value",
                        containsInAnyOrder("String 1", "String 2", "String 3"))));
        assertThat(doc, hasEntry(equalTo("cm_t_scriptstringfield"), hasProperty("value", equalTo("Calculated"))));
        assertThat(doc, hasEntry(equalTo("cm_l_scriptlongfield"), hasProperty("value", equalTo(555L))));
        assertThat(doc, hasEntry(equalTo("cm_dt_scriptdatefield"), hasProperty("value", equalTo(calculatedDate))));
        assertThat(doc, hasEntry(equalTo("cm_b_scriptboolfield"), hasProperty("value", equalTo(true))));
        assertThat(doc, hasEntry(equalTo("cm_ds_scriptfloatarrayfield"), hasProperty("value")));   //*****
    }*/

    @Test
    public void testSaveAttachment() throws Exception {
        // Модель конфигурации области поиска
        TargetDomainObjectConfig objectConfig = mock(TargetDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn("TargetType");
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn("TestArea");
        when(areaConfig.getTargetObjectType()).thenReturn("TargetType");
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);
        when(areaConfig.getObjectConfigChain()).thenReturn(new IndexedDomainObjectConfig[] { objectConfig });

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(true);
        when(configHelper.isSuitableType("TargetType", "TargetType")).thenReturn(true);
        when(configHelper.getAttachmentParentLinkName("TestType", "TargetType")).thenReturn("ParentLink");
        when(configHelper.getSupportedLanguages()).thenReturn(Arrays.asList(""));
        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Модель сохраняемого объекта и его изменений
        DomainObject object = mock(DomainObject.class);
        Id id = idMock("TestId");
        when(object.getId()).thenReturn(id);
        when(object.getTypeName()).thenReturn("TestType");
        when(object.getValue(BaseAttachmentService.NAME)).thenReturn(new StringValue("Attachment name"));
        when(object.getString(BaseAttachmentService.NAME)).thenReturn("Attachment name");
        when(object.getValue(BaseAttachmentService.DESCRIPTION)).thenReturn(new StringValue("Attachment description"));
        when(object.getValue(BaseAttachmentService.CONTENT_LENGTH)).thenReturn(new LongValue(1500L));
        when(object.getModifiedDate()).thenReturn(new Date(55777L));
        Id parentId = idMock("ParentId");
        when(object.getReference("ParentLink")).thenReturn(parentId);
        DomainObject parent = mock(DomainObject.class);
        when(parent.getTypeName()).thenReturn("TargetType");
        
        FieldModification modMock = mock(FieldModification.class);
        when(modMock.getName()).thenReturn(BaseAttachmentService.NAME);
        InputStream contentMock = mock(InputStream.class);
        when(attachmentContentDao.loadContent(Mockito.any(DomainObject.class))).thenReturn(contentMock);
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parent);

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
