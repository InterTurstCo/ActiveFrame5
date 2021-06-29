package ru.intertrust.cm.core.business.impl.search;

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
import ru.intertrust.cm.core.config.search.IndexedFieldScriptConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.ParentLinkConfig;
import ru.intertrust.cm.core.config.search.TargetDomainObjectConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.Subject;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DomainObjectIndexAgentTest {

    @Mock
    private SolrUpdateRequestQueue requestQueue;
    @Mock
    private SearchConfigHelper configHelper;
    @Mock
    private DoelEvaluator doelEvaluator;
    @Mock
    private DomainObjectDao domainObjectDao;
    @Mock
    private ScriptService scriptService;

    @Mock
    private AccessControlService accessControlService;
    @Mock
    private AttachmentContentDao attachmentContentDao;

    @Mock
    private SolrServerWrapperMap solrServerWrapperMap;
    @Mock
    private SolrServerWrapper solrServerWrapper;

    @InjectMocks
    DomainObjectIndexAgent testee = new DomainObjectIndexAgent();

    @Captor
    ArgumentCaptor<Collection<SolrInputDocument>> documents;// = ArgumentCaptor.forClass(Collection.class);

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
        IndexedFieldConfig stringField = getIndexedFieldConfig("StringField");
        IndexedFieldConfig ruStringField = getIndexedFieldConfig("RuStringField");
        IndexedFieldConfig ruEnGeTextField = getIndexedFieldConfig("RuEnGeTextField");
        IndexedFieldConfig longField = getIndexedFieldConfig("LongField");
        IndexedFieldConfig decimalField = getIndexedFieldConfig("DecimalField");
        IndexedFieldConfig dateField = getIndexedFieldConfig("DateField");
        IndexedFieldConfig referenceField = getIndexedFieldConfig("ReferenceField");
        IndexedFieldConfig doelField = getIndexedFieldConfig("DoelField", "doel^multiple.strings");

        IndexedFieldConfig customField = getIndexedFieldConfig("CustomField", null, "custom", null);

        IndexedFieldScriptConfig scriptConfig = mock(IndexedFieldScriptConfig.class);
        when(scriptConfig.getScript()).thenReturn("evaluate");

        IndexedFieldConfig scriptField = getIndexedFieldConfig("ScriptField", scriptConfig);
        
        TargetDomainObjectConfig objectConfig = getTargetDomainObjectConfig("TestType", Arrays.asList(
                stringField, ruStringField, ruEnGeTextField,
                longField, decimalField, dateField, referenceField, doelField, customField,
                scriptField));

        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { objectConfig },
                objectConfig, "TestArea", "TestType");

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Collections.singletonList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType("TestType", "TestType")).thenReturn(true);
        when(configHelper.getFieldTypes(same(stringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList(""))));
        when(configHelper.getFieldTypes(same(ruStringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList("ru"))));
        when(configHelper.getFieldTypes(same(ruEnGeTextField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Arrays.asList("ru", "en", "ge"))));
        when(configHelper.getFieldTypes(same(longField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG)));
        when(configHelper.getFieldTypes(same(decimalField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.DOUBLE)));
        when(configHelper.getFieldTypes(same(dateField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.DATE)));
        when(configHelper.getFieldTypes(same(referenceField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new SimpleSearchFieldType(SimpleSearchFieldType.Type.REF)));
        when(configHelper.getFieldTypes(same(doelField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList(""), true)));
        when(configHelper.getFieldTypes(same(scriptField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList(""))));
        when(configHelper.getFieldTypes(same(customField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new CustomSearchFieldType("custom_")));
        /*when(configHelper.getSupportedLanguages(anyString(), anyString())).thenReturn(Arrays.asList(""));
        when(configHelper.getSupportedLanguages(eq("RuStringField"), anyString())).thenReturn(Arrays.asList("ru"));
        when(configHelper.getSupportedLanguages(eq("RuEnGeTextField"), anyString())).thenReturn(
                Arrays.asList("ru", "en", "ge"));*/
        when(configHelper.getSupportedLanguages()).thenReturn(Collections.singletonList(""));

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

    private IndexedFieldConfig getIndexedFieldConfig(String field, String doel, String solrPrefix, IndexedFieldScriptConfig scriptConfig) {
        IndexedFieldConfig indexedField = mock(IndexedFieldConfig.class);
        when(indexedField.getName()).thenReturn(field);
        when(indexedField.getDoel()).thenReturn(doel);
        when(indexedField.getSolrPrefix()).thenReturn(solrPrefix);
        when(indexedField.getScriptConfig()).thenReturn(scriptConfig);
        return indexedField;
    }

    private IndexedFieldConfig getIndexedFieldConfig(String field, String doel) {
        return getIndexedFieldConfig(field, doel, null, null);
    }

    private IndexedFieldConfig getIndexedFieldConfig(String field, IndexedFieldScriptConfig scriptConfig) {
        return getIndexedFieldConfig(field, null, null, scriptConfig);
    }

    private IndexedFieldConfig getIndexedFieldConfig(String field) {
        return getIndexedFieldConfig(field, null, null, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSaveGenericDocument_LinkedObject() {
        // Модель конфигурации области поиска
        IndexedFieldConfig stringField = getIndexedFieldConfig("TestField");

        ParentLinkConfig parentLinkConfig = getParentLinkConfig("doel.parent.link");

        LinkedDomainObjectConfig objectConfig = getLinkedDomainObjectConfig(parentLinkConfig,
                Collections.singletonList(stringField), "TestType");

        TargetDomainObjectConfig parentObjectConfig = getTargetDomainObjectConfig("TargetType");

        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { objectConfig, parentObjectConfig },
                objectConfig,
                "TestArea", "TargetType"
        );
          
        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Collections.singletonList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType(anyString(), anyString())).thenReturn(true);
        when(configHelper.getFieldTypes(same(stringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList(""))));
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
                Mockito.any(AccessToken.class))).thenReturn(Collections.singletonList((Value) new ReferenceValue(parentId)));
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parent);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Collections.singletonList(modMock));

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

    private ParentLinkConfig getParentLinkConfig(String s) {
        ParentLinkConfig parentLinkConfig = mock(ParentLinkConfig.class);
        when(parentLinkConfig.getDoel()).thenReturn(s);
        return parentLinkConfig;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSaveGenericDocument_IndirectLinkedObject() {
        // Модель конфигурации области поиска
        IndexedFieldConfig stringField = getIndexedFieldConfig("TestField");

        ParentLinkConfig intermedLinkConfig = getParentLinkConfig("doel.intermediate.link");

        LinkedDomainObjectConfig objectConfig =
                getLinkedDomainObjectConfig(intermedLinkConfig, Collections.singletonList(stringField), "TestType");

        ParentLinkConfig parentLinkConfig = getParentLinkConfig("doel.parent.link");

        LinkedDomainObjectConfig intermediateConfig = getLinkedDomainObjectConfig(parentLinkConfig, "IntermediateType");

        TargetDomainObjectConfig parentObjectConfig = getTargetDomainObjectConfig("TargetType");
        
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { objectConfig, intermediateConfig, parentObjectConfig },
                objectConfig, "TestArea", "TargetType"
        );
          
        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Collections.singletonList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType(anyString(), anyString())).thenReturn(true);
        when(configHelper.getFieldTypes(same(stringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList(""))));
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
                Mockito.any(AccessToken.class))).thenReturn(Collections.singletonList((Value) new ReferenceValue(intermediateId)));
        when(doelEvaluator.evaluate(eq(DoelExpression.parse("doel.parent.link")), same(intermediateId),
                Mockito.any(AccessToken.class))).thenReturn(Collections.singletonList((Value) new ReferenceValue(parentId)));
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(intermediateId, mockToken)).thenReturn(intermediate);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parent);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Collections.singletonList(modMock));

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
        IndexedFieldConfig stringField = getIndexedFieldConfig("TestField");

        ParentLinkConfig parentLinkConfig = getParentLinkConfig("doel.parent.link");

        LinkedDomainObjectConfig objectConfig = getLinkedDomainObjectConfig(parentLinkConfig,
                Collections.singletonList(stringField), "TestType");

        TargetDomainObjectConfig parentObjectConfig = getTargetDomainObjectConfig("TargetType");
        
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { objectConfig, parentObjectConfig },
                objectConfig,
                "TestArea", "TargetType"
        );
        

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Collections.singletonList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType(anyString(), anyString())).thenReturn(true);
        when(configHelper.getFieldTypes(same(stringField), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList(""))));
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
                Mockito.any(AccessToken.class))).thenReturn(Collections.emptyList());
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Collections.singletonList(modMock));

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
        IndexedFieldConfig field = getIndexedFieldConfig("TestField");

        ParentLinkConfig parentLink = getParentLinkConfig("doel.parent.link");

        TargetDomainObjectConfig parentFit = getTargetDomainObjectConfig("ParentFit");

        LinkedDomainObjectConfig configFit = getLinkedDomainObjectConfig(parentLink,
                Collections.singletonList(field), "TestType");

        SearchConfigHelper.SearchAreaDetailsConfig areaFit = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { configFit, parentFit },
                configFit, "AreaFit", "ParentFit"
        );
                
        TargetDomainObjectConfig parentNotFit = getTargetDomainObjectConfig("ParentNotFit");

        LinkedDomainObjectConfig configNotFit = getLinkedDomainObjectConfig(parentLink,
                Collections.singletonList(field), "TestType");

        SearchConfigHelper.SearchAreaDetailsConfig areaNotFit = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[]{configNotFit, parentNotFit},
                configNotFit, "AreaNotFit", "ParentNotFit"
        );
                
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
                Mockito.any(AccessToken.class))).thenReturn(Collections.singletonList((Value) new ReferenceValue(parentId)));
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parentObject);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Collections.singletonList(modMock));

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

    private LinkedDomainObjectConfig getLinkedDomainObjectConfig(ParentLinkConfig parentLink, String type) {
        return getLinkedDomainObjectConfig(parentLink, Collections.emptyList(), type);
    }

    private LinkedDomainObjectConfig getLinkedDomainObjectConfig(ParentLinkConfig parentLink, List<IndexedFieldConfig> indexedFieldConfigs, String type) {
        return getLinkedDomainObjectConfig(parentLink, indexedFieldConfigs, type, false);
    }

    private LinkedDomainObjectConfig getLinkedDomainObjectConfig(ParentLinkConfig parentLink, List<IndexedFieldConfig> indexedFieldConfigs, String type,
                                                                 boolean nested) {
        LinkedDomainObjectConfig configFit = mock(LinkedDomainObjectConfig.class);
        when(configFit.getType()).thenReturn(type);
        when(configFit.getFields()).thenReturn(indexedFieldConfigs);
        when(configFit.getParentLink()).thenReturn(parentLink);
        when(configFit.isNested()).thenReturn(nested);
        return configFit;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSaveGenericDocument_MultipleAreasDifferentConfigs() {
        // Модель конфигурации областей поиска
        IndexedFieldConfig field1 = getIndexedFieldConfig("FieldA");

        TargetDomainObjectConfig config1 = getTargetDomainObjectConfig("TestType", Collections.singletonList(field1));

        SearchConfigHelper.SearchAreaDetailsConfig area1 = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { config1 },
                config1, "AreaA", "TestType");

        IndexedFieldConfig field2 = getIndexedFieldConfig("FieldB");

        ParentLinkConfig parent = getParentLinkConfig("doel.parent.link");

        LinkedDomainObjectConfig config2 = getLinkedDomainObjectConfig(parent,
                Collections.singletonList(field2), "TestType");

        TargetDomainObjectConfig parentObjectConfig = getTargetDomainObjectConfig("ParentType");

        SearchConfigHelper.SearchAreaDetailsConfig area2 = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { config2, parentObjectConfig },
                config2,
                "AreaA", "ParentType"
        );

        IndexedFieldConfig field3a = getIndexedFieldConfig("FieldB");

        IndexedFieldConfig field3b = getIndexedFieldConfig("FieldC");

        TargetDomainObjectConfig config3 = getTargetDomainObjectConfig("TestType");
        when(config3.getFields()).thenReturn(Arrays.asList(field3a, field3b));

        SearchConfigHelper.SearchAreaDetailsConfig area3 = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { config3 },
                config3, "AreaB", "TestType");

        IndexedFieldConfig field4 = getIndexedFieldConfig("FieldD");

        TargetDomainObjectConfig config4 = getTargetDomainObjectConfig("TestType");
        when(config4.getFields()).thenReturn(Collections.singletonList(field4));

        SearchConfigHelper.SearchAreaDetailsConfig area4 = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { config4 },
                config4, "AreaC", "TestType");

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Arrays.asList(area1, area2, area3, area4));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType(anyString(), anyString())).thenReturn(true);
        when(configHelper.getFieldTypes(Mockito.any(IndexedFieldConfig.class), anyString())).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList(""))));
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
                Mockito.any(AccessToken.class))).thenReturn(Collections.singletonList((Value) new ReferenceValue(parentId)));
        when(domainObjectDao.find(id, mockToken)).thenReturn(object);
        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parentObject);

        // Вызов тестируемого метода
        testee.onAfterSave(object, Collections.singletonList(modMock));

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
        IndexedFieldConfig childTypeField = getIndexedFieldConfig("ChildTypeField");

        TargetDomainObjectConfig objectConfig = getTargetDomainObjectConfig("ParentType", Collections.singletonList(childTypeField));

        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { objectConfig },
                objectConfig, "TestArea", "ParentType");
        
        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Collections.singletonList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType("ParentType", "ChildType")).thenReturn(true);
        when(configHelper.getFieldTypes(same(childTypeField), eq("ChildType"))).thenReturn(
                Collections.<SearchFieldType>singleton(new TextSearchFieldType(Collections.singletonList(""))));
        when(configHelper.getSupportedLanguages()).thenReturn(Collections.singletonList(""));

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
        testee.onAfterSave(object, Collections.singletonList(modMock));

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

    @Test
    public void testSaveAttachment() throws Exception {
        // Модель конфигурации области поиска
        TargetDomainObjectConfig objectConfig = getTargetDomainObjectConfig("TargetType");

        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[] { objectConfig }, 
                objectConfig, "TestArea", "TargetType");

        when(configHelper.findEffectiveConfigs(Mockito.anyString()))
                .thenReturn(Collections.singletonList(areaConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(true);
        when(configHelper.isSuitableType("TargetType", "TargetType")).thenReturn(true);
        when(configHelper.getAttachmentParentLinkName("TestType", "TargetType")).thenReturn("ParentLink");
        when(configHelper.getSupportedLanguages()).thenReturn(Collections.singletonList(""));
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
        testee.onAfterSave(object, Collections.singletonList(modMock));

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

    private SearchConfigHelper.SearchAreaDetailsConfig getSearchAreaDetailsConfig(IndexedDomainObjectConfig[] configChain,
                                                                                  IndexedDomainObjectConfig objectConfig,
                                                                                  String testArea, String targetType) {
        
        SearchConfigHelper.SearchAreaDetailsConfig areaConfig = mock(SearchConfigHelper.SearchAreaDetailsConfig.class);
        when(areaConfig.getAreaName()).thenReturn(testArea);
        when(areaConfig.getTargetObjectType()).thenReturn(targetType);
        when(areaConfig.getObjectConfig()).thenReturn(objectConfig);
        when(areaConfig.getObjectConfigChain()).thenReturn(configChain);
        return areaConfig;
    }

    private TargetDomainObjectConfig getTargetDomainObjectConfig(String targetType) {
        return getTargetDomainObjectConfig(targetType, Collections.emptyList());
    }

    private TargetDomainObjectConfig getTargetDomainObjectConfig(String targetType, List<IndexedFieldConfig> childConfig) {
        return getTargetDomainObjectConfig(targetType, childConfig, Collections.emptyList());
    }

    private TargetDomainObjectConfig getTargetDomainObjectConfig(String targetType,
                                                                 List<IndexedFieldConfig> childConfig,
                                                                 List<LinkedDomainObjectConfig> linkedDomainObjectConfigs) {
        TargetDomainObjectConfig objectConfig = mock(TargetDomainObjectConfig.class);
        when(objectConfig.getType()).thenReturn(targetType);
        when(objectConfig.getFields()).thenReturn(childConfig);
        when(objectConfig.getLinkedObjects()).thenReturn(linkedDomainObjectConfigs);
        
        return objectConfig;
    }

    private Id idMock(String stringRepresentation) {
        Id id = mock(Id.class);
        when(id.toStringRepresentation()).thenReturn(stringRepresentation);
        return id;
    }


    //  Test1 - target (можно без полей) + 2 nested linked у каждого по 1 полю.
    //  Проверяем, что они запишутся в 1 документ + системные поля
    @Test
    public void multipleNestedLinkedDocs() {

        // Собираем конфиги
        IndexedFieldConfig stringField1 = getIndexedFieldConfig("StringField1");
        ParentLinkConfig parentLinkConfig1 = getParentLinkConfig("doel.parent.link1");
        LinkedDomainObjectConfig linkedDomainObjectConfig1 = getLinkedDomainObjectConfig(parentLinkConfig1,
                Collections.singletonList(stringField1),
                "TestType1");

        SearchConfigHelper.SearchAreaDetailsConfig forLinked1 = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[]{ linkedDomainObjectConfig1 },
                linkedDomainObjectConfig1, "testArea", "StringField1");

        IndexedFieldConfig stringField2 = getIndexedFieldConfig("StringField2");
        ParentLinkConfig parentLinkConfig2 = getParentLinkConfig("doel.parent.link2");
        LinkedDomainObjectConfig linkedDomainObjectConfig2 = getLinkedDomainObjectConfig(parentLinkConfig2,
                Collections.singletonList(stringField2),
                "TestType2");

        SearchConfigHelper.SearchAreaDetailsConfig forLinked2 = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[]{ linkedDomainObjectConfig2 },
                linkedDomainObjectConfig2, "testArea", "StringField2");

        TargetDomainObjectConfig testTarget = getTargetDomainObjectConfig("TargetType",
                Collections.emptyList(), Arrays.asList(linkedDomainObjectConfig1, linkedDomainObjectConfig2));

        SearchConfigHelper.SearchAreaDetailsConfig searchAreaDetailsConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[]{ testTarget },
                testTarget, "testArea", "TargetType");

        when(configHelper.findEffectiveConfigs(anyString())).thenReturn(Collections.singletonList(searchAreaDetailsConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType("TargetType", "TargetType")).thenReturn(true);

        when(configHelper.findChildConfigs(searchAreaDetailsConfig, true))
                .thenReturn(Arrays.asList(forLinked1, forLinked2));

        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Собираем объекты и настраиваем связи

        Id parentId = idMock("ParentId");
        DomainObject parent = mock(DomainObject.class);
        when(parent.getTypeName()).thenReturn("TargetType");
        when(parent.getId()).thenReturn(parentId);

        DomainObject linked1 = mock(DomainObject.class);
        Id id1 = idMock("linked1");
        when(linked1.getId()).thenReturn(id1);
        when(linked1.getTypeName()).thenReturn("TestType1");
        when(linked1.getModifiedDate()).thenReturn(new Date(55777L));
        when(linked1.getValue("StringField1")).thenReturn(new StringValue("Value 1"));
        when(linked1.getReference("ParentLink")).thenReturn(parentId);

        DomainObject linked2 = mock(DomainObject.class);
        Id id2 = idMock("linked2");
        when(linked2.getId()).thenReturn(id2);
        when(linked2.getTypeName()).thenReturn("TestType2");
        when(linked2.getModifiedDate()).thenReturn(new Date(5777L));
        when(linked2.getValue("StringField2")).thenReturn(new StringValue("Value 2"));
        when(linked2.getReference("ParentLink")).thenReturn(parentId);

        when(domainObjectDao.find(anyList(), eq(mockToken))).then(invocationOnMock -> {
            List list = invocationOnMock.getArgumentAt(0, List.class);
            Id id = (Id) list.get(0);
            if (id == id1) {
                return Collections.singletonList(linked1);
            } else if (id == id2) {
                return Collections.singletonList(linked2);
            }

            throw new RuntimeException();
        });

        TextSearchFieldType searchFieldType1 = new TextSearchFieldType(Collections.singletonList("ru"), true);
        when(configHelper.getFieldTypes(stringField1, "TestType1"))
                .thenReturn(Collections.singleton(searchFieldType1));


        when(configHelper.getFieldTypes(stringField2, "TestType2"))
                .thenReturn(Collections.singleton(searchFieldType1));

        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parent);

        DoelExpression doelExpressionForLink1 = mock(DoelExpression.class);
        when(doelEvaluator.createReverseExpression(
                DoelExpression.parse("doel.parent.link1"),
                "TestType1",
                true, null)).thenReturn(doelExpressionForLink1);

        when(doelEvaluator
                .evaluate(eq(doelExpressionForLink1), same(parentId), Mockito.any(AccessToken.class)))
                .thenReturn(Collections.singletonList(new ReferenceValue(id1)));

        DoelExpression doelExpressionForLink2 = mock(DoelExpression.class);
        when(doelEvaluator.createReverseExpression(
                DoelExpression.parse("doel.parent.link2"),
                "TestType2",
                true, null)).thenReturn(doelExpressionForLink2);

        when(doelEvaluator
                .evaluate(eq(doelExpressionForLink2), same(parentId), Mockito.any(AccessToken.class)))
                .thenReturn(Collections.singletonList(new ReferenceValue(id2)));

        testee.index(parent);

        ArgumentCaptor<Collection> documents = ArgumentCaptor.forClass(Collection.class);
        verify(requestQueue).addDocuments(documents.capture());
        assertEquals(1, documents.getValue().size());
        SolrInputDocument doc = (SolrInputDocument) documents.getValue().iterator().next();
        assertThat(doc, hasEntry(equalTo("cm_id"), hasProperty("value", equalTo("ParentId"))));
        assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("testArea"))));
        assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("TargetType"))));
        assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("ParentId"))));
        assertThat(doc, hasEntry(equalTo("cm_rus_stringfield1"), hasProperty("value", equalTo("Value 1"))));
        assertThat(doc, hasEntry(equalTo("cm_rus_stringfield2"), hasProperty("value", equalTo("Value 2"))));
    }

    @Test
    public void nestedConfigDoesntProcess() {
        IndexedFieldConfig stringField1 = getIndexedFieldConfig("StringField1");

        ParentLinkConfig parentLinkConfig1 = getParentLinkConfig("doel.parent.link1");
        LinkedDomainObjectConfig linkedDomainObjectConfig1 = getLinkedDomainObjectConfig(parentLinkConfig1,
                Collections.singletonList(stringField1),
                "TestType1", true);

        SearchConfigHelper.SearchAreaDetailsConfig searchAreaDetailsConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[]{ linkedDomainObjectConfig1 },
                linkedDomainObjectConfig1, "testArea", "StringField1");

        DomainObject domainObject = mock(DomainObject.class);
        Id id1 = idMock("linked1");
        when(domainObject.getId()).thenReturn(id1);
        when(domainObject.getTypeName()).thenReturn("TestType1");
        when(domainObject.getModifiedDate()).thenReturn(new Date(55777L));


        when(configHelper.findEffectiveConfigs(anyString())).thenReturn(Collections.singletonList(searchAreaDetailsConfig));

        testee.index(domainObject);

        verify(requestQueue, never()).addDocuments(any());
    }

//    Test3 - похож на test1, но поля должны схлопнуться в список (multivalued) + там вместо разных linked - один с 2 полями
    @Test
    public void multipleNestedLinkedDocs_multipalValues() {

        // Собираем конфиги
        IndexedFieldConfig stringField1 = getIndexedFieldConfig("StringField1");
        ParentLinkConfig parentLinkConfig1 = getParentLinkConfig("doel.parent.link1");
        LinkedDomainObjectConfig linkedDomainObjectConfig1 = getLinkedDomainObjectConfig(parentLinkConfig1,
                Collections.singletonList(stringField1),
                "TestType1");

        SearchConfigHelper.SearchAreaDetailsConfig forLinked1 = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[]{ linkedDomainObjectConfig1 },
                linkedDomainObjectConfig1, "testArea", "StringField1");

        TargetDomainObjectConfig testTarget = getTargetDomainObjectConfig("TargetType",
                Collections.emptyList(), Collections.singletonList(linkedDomainObjectConfig1));

        SearchConfigHelper.SearchAreaDetailsConfig searchAreaDetailsConfig = getSearchAreaDetailsConfig(
                new IndexedDomainObjectConfig[]{ testTarget },
                testTarget, "testArea", "TargetType");

        when(configHelper.findEffectiveConfigs(anyString())).thenReturn(Collections.singletonList(searchAreaDetailsConfig));
        when(configHelper.isAttachmentObject(Mockito.any(DomainObject.class))).thenReturn(false);
        when(configHelper.isSuitableType("TargetType", "TargetType")).thenReturn(true);

        when(configHelper.findChildConfigs(searchAreaDetailsConfig, true))
                .thenReturn(Collections.singletonList(forLinked1));

        when(accessControlService.createSystemAccessToken(anyString())).thenReturn(mockToken);

        // Собираем объекты и настраиваем связи

        Id parentId = idMock("ParentId");
        DomainObject parent = mock(DomainObject.class);
        when(parent.getTypeName()).thenReturn("TargetType");
        when(parent.getId()).thenReturn(parentId);

        DomainObject linked1 = mock(DomainObject.class);
        Id id1 = idMock("linked1");
        when(linked1.getId()).thenReturn(id1);
        when(linked1.getTypeName()).thenReturn("TestType1");
        when(linked1.getModifiedDate()).thenReturn(new Date(55777L));
        when(linked1.getValue("StringField1")).thenReturn(new StringValue("Value 1"));
        when(linked1.getReference("ParentLink")).thenReturn(parentId);

        DomainObject linked2 = mock(DomainObject.class);
        Id id2 = idMock("linked2");
        when(linked2.getId()).thenReturn(id2);
        when(linked2.getTypeName()).thenReturn("TestType1");
        when(linked2.getModifiedDate()).thenReturn(new Date(5777L));
        when(linked2.getValue("StringField1")).thenReturn(new StringValue("Value 2"));
        when(linked2.getReference("ParentLink")).thenReturn(parentId);

        when(domainObjectDao.find(anyList(), eq(mockToken))).then(invocationOnMock -> {
            List list = invocationOnMock.getArgumentAt(0, List.class);
            List<DomainObject> dObjs =new ArrayList<>(2);
            for (Object id : list) {
                if (id == id1) {
                    dObjs.add(linked1);
                } else if (id == id2) {
                    dObjs.add(linked2);
                }
            }
            return dObjs;
        });

        TextSearchFieldType searchFieldType1 = new TextSearchFieldType(Collections.singletonList("ru"), true);
        when(configHelper.getFieldTypes(stringField1, "TestType1"))
                .thenReturn(Collections.singleton(searchFieldType1));

        when(domainObjectDao.find(parentId, mockToken)).thenReturn(parent);

        DoelExpression doelExpressionForLink1 = mock(DoelExpression.class);
        when(doelEvaluator.createReverseExpression(
                DoelExpression.parse("doel.parent.link1"),
                "TestType1",
                true, null)).thenReturn(doelExpressionForLink1);

        when(doelEvaluator
                .evaluate(eq(doelExpressionForLink1), same(parentId), Mockito.any(AccessToken.class)))
                .thenReturn(Arrays.asList(new ReferenceValue(id1), new ReferenceValue(id2)));

        testee.index(parent);

        ArgumentCaptor<Collection> documents = ArgumentCaptor.forClass(Collection.class);
        verify(requestQueue).addDocuments(documents.capture());
        assertEquals(1, documents.getValue().size());
        SolrInputDocument doc = (SolrInputDocument) documents.getValue().iterator().next();
        assertThat(doc, hasEntry(equalTo("cm_id"), hasProperty("value", equalTo("ParentId"))));
        assertThat(doc, hasEntry(equalTo("cm_area"), hasProperty("value", equalTo("testArea"))));
        assertThat(doc, hasEntry(equalTo("cm_type"), hasProperty("value", equalTo("TargetType"))));
        assertThat(doc, hasEntry(equalTo("cm_main"), hasProperty("value", equalTo("ParentId"))));
        assertEquals( Arrays.asList("Value 1", "Value 2"), doc.getFieldValues("cm_rus_stringfield1"));
    }
}
