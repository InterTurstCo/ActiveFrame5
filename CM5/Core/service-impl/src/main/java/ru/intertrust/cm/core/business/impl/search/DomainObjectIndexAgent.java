package ru.intertrust.cm.core.business.impl.search;

import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.ContentStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.ScriptService;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.ParentLinkConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.model.DoelException;
import ru.intertrust.cm.core.tools.SearchAreaFilterScriptContext;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Компонент, осуществляющий индексацию доменных объектов при их изменении.
 * Устанавливается как обработчик точки расширения после сохранения, а также удаления доменного объекта
 * ({@link AfterSaveAfterCommitExtensionHandler}, {@link AfterDeleteAfterCommitExtensionHandler}).
 * <p>Компонент определяет все области поиска, в которых должен проиндексироваться изменённый объект,
 * формирует поисковые запросы с использованием Solrj, но не выполняет обращение к серверу Solr.
 * Вместо этого запросы добавляются в очередь, обслуживаемую экземпляром класса {@link SolrUpdateRequestQueue},
 * из которой извлекаются в асинхронном режиме задачей, работающей по расписанию - см. {@link SolrIndexingBean}.
 * 
 * @author apirozhkov
 */
@ExtensionPoint
public class DomainObjectIndexAgent implements AfterSaveAfterCommitExtensionHandler, AfterDeleteAfterCommitExtensionHandler {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SolrUpdateRequestQueue requestQueue;

    @Autowired
    private SearchConfigHelper configHelper;

    @Autowired
    private DoelEvaluator doelEvaluator;

    @Autowired
    private DomainObjectDao domainObjectDao;
    
    @Autowired
    private AccessControlService accessControlService;
    
    @Autowired
    private ScriptService scriptService;    

    @Autowired
    private AttachmentContentDao attachmentContentDao;

    @org.springframework.beans.factory.annotation.Value("${attachment.index.exclusion:avi,asf,mpg,mpeg,mpe,vob,mp4,m4v,3gp,3gpp,flv,swf,mov,divx,webm,wav,wma,mp3,ogg,aac,ac3,jpg,jpeg,bmp}")
    private String attachmentIndexExclusionConfig;

    private Set<String> exclusionSet = new HashSet<>();

    @PostConstruct
    public void init(){
        if (attachmentIndexExclusionConfig != null){
            String[] attachmentIndexExclusionConfigArray = attachmentIndexExclusionConfig.split("[,;\t]");
            for (String ignoreExtension : attachmentIndexExclusionConfigArray) {
                exclusionSet.add(ignoreExtension.toLowerCase().trim());
            }
        }
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        // Проверка включения агента индексирования
        if (configHelper.isDisableIndexing()){
            return;
        }

        List<SearchConfigHelper.SearchAreaDetailsConfig> configs =
                configHelper.findEffectiveConfigs(domainObject.getTypeName());
        if (configs.size() == 0) {
            return;
        }
        ArrayList<SolrInputDocument> solrDocs = new ArrayList<>(configs.size());
        ArrayList<String> toDelete = new ArrayList<>();
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            if (configHelper.isAttachmentObject(domainObject)) {
                sendAttachment(domainObject, config);
                continue;
            }
            List<Id> mainIds = calculateMainObjects(domainObject.getId(), config.getObjectConfigChain());
            if (mainIds.size() > 0) {
                reindexObjectAndChildren(solrDocs, domainObject, config, mainIds);
            } else {
                toDelete.add(createUniqueId(domainObject, config));
            }
        }
        if (solrDocs.size() > 0) {
            requestQueue.addDocuments(solrDocs);
            if (log.isInfoEnabled()) {
                log.info("" + solrDocs.size() + " Solr document(s) queued for indexing");
            }
        }
        if (toDelete.size() > 0) {
            requestQueue.addRequest(new UpdateRequest().deleteById(toDelete));
            if (log.isInfoEnabled()) {
                log.info("" + toDelete.size() + " Solr document(s) queued for deleting");
            }
        }
    }

    private void reindexObjectAndChildren(List<SolrInputDocument> solrDocs, DomainObject object,
            SearchConfigHelper.SearchAreaDetailsConfig config, List<Id> mainIds) {
        for (Id mainId : mainIds) {
            SolrInputDocument doc = new SolrInputDocument();

            // System fields
            doc.addField(SolrFields.OBJECT_ID, object.getId().toStringRepresentation());
            doc.addField(SolrFields.AREA, config.getAreaName());
            doc.addField(SolrFields.TARGET_TYPE, config.getTargetObjectType());
            doc.addField(SolrFields.OBJECT_TYPE, config.getObjectConfig().getType());
            doc.addField(SolrFields.MAIN_OBJECT_ID, mainId.toStringRepresentation());
            doc.addField(SolrFields.MODIFIED, object.getModifiedDate());

            // Business fields
            for (IndexedFieldConfig fieldConfig : config.getObjectConfig().getFields()) {
                Map<SearchFieldType, ?> values = calculateField(object, fieldConfig);
                for (Map.Entry<SearchFieldType, ?> entry : values.entrySet()) {
                    SearchFieldType type = entry.getKey();
                    for (String fieldName : type.getSolrFieldNames(fieldConfig.getName(), true)) {
                        doc.addField(fieldName, entry.getValue());
                    }
                }
            }
            doc.addField("id", createUniqueId(object, config));
            solrDocs.add(doc);
        }

        for (SearchConfigHelper.SearchAreaDetailsConfig linkedConfig : configHelper.findChildConfigs(config)) {
            LinkedDomainObjectConfig cfg = (LinkedDomainObjectConfig) linkedConfig.getObjectConfig();
            if (LinkedDomainObjectConfig.REINDEX_ON_CHANGE.equalsIgnoreCase(cfg.getReindexOnParent())
                    || LinkedDomainObjectConfig.REINDEX_ON_CREATE.equalsIgnoreCase(cfg.getReindexOnParent())
                    && object.getCreatedDate().equals(object.getModifiedDate())) {
                for (DomainObject child : findChildren(object.getId(), linkedConfig)) {
                    reindexObjectAndChildren(solrDocs, child, linkedConfig, mainIds);
                }
            }
        }
    }

    private List<DomainObject> findChildren(Id objectId, SearchConfigHelper.SearchAreaDetailsConfig config) {
        String parentLink = ((LinkedDomainObjectConfig) config.getObjectConfig()).getParentLink().getDoel();
        DoelExpression parentExpr = DoelExpression.parse(parentLink);
        DoelExpression linkedExpr;
        try {
            linkedExpr = doelEvaluator.createReverseExpression(parentExpr, config.getObjectConfig().getType());
        } catch (DoelException e) {
            log.warn("Can't calculate children of type " + config.getObjectConfig().getType() + ": " + e.getMessage()
                    + "; manual/scheduled calculation required");
            return Collections.emptyList();
        }
        AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
        List<ReferenceValue> children = doelEvaluator.evaluate(linkedExpr, objectId, accessToken);
        if (children.size() == 0) {
            return Collections.emptyList();
        }
        ArrayList<Id> ids = new ArrayList<>(children.size());
        for (ReferenceValue child : children) {
            if (child.get() != null) {
                ids.add(child.get());
            }
        }
        return domainObjectDao.find(ids, accessToken);
    }

    private void sendAttachment(DomainObject object, SearchConfigHelper.SearchAreaDetailsConfig config) {
        // Проверка на то что данный тип вложения надо индексировать
        if (needIndex(object)) {
            String linkName = configHelper.getAttachmentParentLinkName(object.getTypeName(),
                    config.getObjectConfig().getType());
            List<Id> mainIds = calculateMainObjects(object.getReference(linkName), config.getObjectConfigChain());
            for (Id mainId : mainIds) {
                ContentStreamUpdateRequest request = new ContentStreamUpdateRequest("/update/extract");
                request.addContentStream(new SolrAttachmentFeeder(object));
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrFields.OBJECT_ID, object.getId().toStringRepresentation());
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrFields.AREA, config.getAreaName());
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrFields.TARGET_TYPE, config.getTargetObjectType());
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrFields.OBJECT_TYPE, config.getObjectConfig().getType());
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrFields.MAIN_OBJECT_ID, mainId.toStringRepresentation());
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrFields.MODIFIED,
                        ThreadSafeDateFormat.format(object.getModifiedDate(), DATE_PATTERN));
                addFieldToContentRequest(request, object, BaseAttachmentService.NAME,
                        new TextSearchFieldType(configHelper.getSupportedLanguages(), false, false));
                addFieldToContentRequest(request, object, BaseAttachmentService.DESCRIPTION,
                        new TextSearchFieldType(configHelper.getSupportedLanguages(), false, false));
                addFieldToContentRequest(request, object, BaseAttachmentService.CONTENT_LENGTH,
                        new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG));
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrUtils.ID_FIELD, createUniqueId(object, config));
                request.setParam("uprefix", "cm_c_");
                request.setParam("fmap.content", SolrFields.CONTENT);
                //request.setParam("extractOnly", "true");

                requestQueue.addRequest(request);
            }
            if (log.isInfoEnabled()) {
                log.info("Attachment queued for indexing");
            }
        }else{
            log.debug("Indexing attachment " + object.getString("name") + " is ignored by file extension");
        }
    }

    private boolean needIndex(DomainObject object) {
        String name = object.getString(BaseAttachmentService.NAME);
        // Не индексируем вложения без имени
        if (name == null){
            return false;
        }
        // Проверка расширение файла на исключения из индексирования
        String extension = FilenameUtils.getExtension(name).toLowerCase();
        return !exclusionSet.contains(extension);
    }

    private void addFieldToContentRequest(ContentStreamUpdateRequest request,
            DomainObject object, String fieldName, SearchFieldType fieldType) {
        Object value = convertValue(object.getValue(fieldName));
        if (value != null) {
            for (String solrField : fieldType.getSolrFieldNames(fieldName, true)) {
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + solrField, value.toString());
            }
        }
    }

    private Map<SearchFieldType, ?> calculateField(DomainObject object, IndexedFieldConfig config) {
        try {
            Collection<SearchFieldType> types = configHelper.getFieldTypes(config, object.getTypeName());
            if (types.size() == 0) {
                return Collections.emptyMap();
            }

            if (config.getScript() != null) {
                SearchAreaFilterScriptContext context = new SearchAreaFilterScriptContext(object);
                Object value = scriptService.eval(config.getScript(), context);
                return Collections.singletonMap(types.iterator().next(), value);

            } else if (config.getDoel() != null) {
                DoelExpression doel = DoelExpression.parse(config.getDoel());
                AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
                List<Value> values = doelEvaluator.evaluate(doel, object.getId(), accessToken);
                if (values.size() == 0) {
                    return Collections.emptyMap();
                }
                // sort values by types
                Map<SearchFieldType, Object> result = new HashMap<>();
                for (Value<?> value : values) {
                    SimpleSearchFieldType.Type typeId = SimpleSearchFieldType.byFieldType(FieldType.find(value.getClass()));
                    for (SearchFieldType type : types) {
                        if (typeId == null && type instanceof TextSearchFieldType) {
                            if (!((TextSearchFieldType) type).isMultiValued()) {
                                result.put(type, convertValue(value));
                            } else {
                                @SuppressWarnings("unchecked")
                                List<Object> list = (List<Object>) result.get(type);
                                if (list == null) {
                                    result.put(type, list = new ArrayList<>());
                                }
                                list.add(convertValue(value));
                            }
                        } else if (type instanceof SimpleSearchFieldType && ((SimpleSearchFieldType) type).type == typeId) {
                            if (!((SimpleSearchFieldType) type).multiValued) {
                                result.put(type, convertValue(value));
                            } else {
                                @SuppressWarnings("unchecked")
                                List<Object> list = (List<Object>) result.get(type);
                                if (list == null) {
                                    result.put(type, list = new ArrayList<>());
                                }
                                list.add(convertValue(value));
                            }
                        }
                    }
                }
                return result;

            } else {
                Object value = convertValue(object.getValue(config.getName()));
                return Collections.singletonMap(types.iterator().next(), value);
            }

        } catch (Exception e) {
            StringBuilder message = new StringBuilder("Field ").append(config.getName()).append(" calculation error");
            if (config.getScript() != null) {
                message.append(" [script=").append(config.getScript()).append("]");
            }
            if (config.getDoel() != null) {
                message.append(" [doel=").append(config.getDoel()).append("]");
            }
            log.error(message.toString(), e);
            return Collections.emptyMap();
        }
    }

    private List<Id> calculateMainObjects(Id objectId, IndexedDomainObjectConfig[] configChain) {
        AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
        ArrayList<Id> ids = new ArrayList<>();
        ids.add(objectId);
        for (IndexedDomainObjectConfig config : configChain) {
            ParentLinkConfig parentConfig = null;
            if (LinkedDomainObjectConfig.class.isAssignableFrom(config.getClass())) {
                parentConfig = ((LinkedDomainObjectConfig) config).getParentLink();
            }
            ArrayList<ReferenceValue> refs = new ArrayList<>();
            for (Iterator<Id> itr = ids.iterator(); itr.hasNext(); ) {
                Id id = itr.next();
                DomainObject object = domainObjectDao.find(id, accessToken);
                if (!configHelper.isSuitableType(config.getType(), object.getTypeName())) {
                    itr.remove();
                    continue;
                }
                DomainObjectFilter filter = configHelper.createFilter(config);
                if (filter != null && !filter.filter(object)) {
                    itr.remove();
                    continue;
                }
                if (parentConfig != null) {
                    List<ReferenceValue> values = doelEvaluator.evaluate(
                            DoelExpression.parse(parentConfig.getDoel()), id, accessToken);
                    refs.addAll(values);
                }
            }
            if (parentConfig == null) {
                return ids;
            }
            if (refs.size() == 0) {
                return Collections.emptyList();
            }
            ids = new ArrayList<>(refs.size());
            for (ReferenceValue ref : refs) {
                ids.add(ref.get());
            }
        }
        return ids;
    }

    private Object convertValue(Value<?> value) {
        if (value == null) {
            return null;
        }
        Object result = null;
        if (value instanceof ReferenceValue) {
            Id id = ((ReferenceValue) value).get();
            result = (id == null) ? null : id.toStringRepresentation();
        } else if (value instanceof TimelessDateValue) {
            TimelessDate date = ((TimelessDateValue) value).getValue();
            if (date != null) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.clear();
                cal.set(date.getYear(), date.getMonth(), date.getDayOfMonth());
                result = cal.getTime();
            }
        } else if (value instanceof DateTimeWithTimeZoneValue) {
            DateTimeWithTimeZone date = ((DateTimeWithTimeZoneValue) value).get();
            if (date != null) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(date.getTimeZoneContext().getTimeZoneId()));
                cal.set(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                        date.getHours(), date.getMinutes(), date.getSeconds());
                result = cal.getTime();
            }
        } else {
            result = value.get();
        }
        return result;
    }

    private String createUniqueId(DomainObject object, SearchConfigHelper.SearchAreaDetailsConfig config) {
        StringBuilder buf = new StringBuilder();
        buf.append(object.getId().toStringRepresentation())
           .append(":").append(config.getAreaName())
           .append(":").append(config.getTargetObjectType());
        return buf.toString();
    }
/*
    private SearchFieldType getTypeByValue(Object value) {
        if (value != null && value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            return getTypeByValue(array.length == 0 ? null : array[0], true);
        }
        if (value instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) value;
            return getTypeByValue(coll.size() == 0 ? null : coll.iterator().next(), true);
        }
        return getTypeByValue(value, false);
    }

    private SearchFieldType getTypeByValue(Object value, boolean multiple) {
        if (value == null || value instanceof String) {
            return new TextSearchFieldType(configHelper.getSupportedLanguages(), multiple, false);
        }
        if (value instanceof Long || value instanceof Integer || value instanceof Byte) {
            return new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG, multiple);
        }
        if (value instanceof Date || value instanceof Calendar) {
            return new SimpleSearchFieldType(SimpleSearchFieldType.Type.DATE, multiple);
        }
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            return new SimpleSearchFieldType(SimpleSearchFieldType.Type.DOUBLE, multiple);
        }
        if (value instanceof Boolean) {
            return new SimpleSearchFieldType(SimpleSearchFieldType.Type.BOOL, multiple);
        }
        return new TextSearchFieldType(configHelper.getSupportedLanguages(), multiple, false);    //*****
    }
*/
    public class SolrAttachmentFeeder implements ContentStream {

        private DomainObject attachment;

        public SolrAttachmentFeeder(DomainObject attachment) {
            this.attachment = attachment;
        }

        @Override
        public String getName() {
            return attachment.getString(BaseAttachmentService.NAME);
        }

        @Override
        public String getSourceInfo() {
            return attachment.getString(BaseAttachmentService.DESCRIPTION);
        }

        @Override
        public String getContentType() {
            return attachment.getString(BaseAttachmentService.MIME_TYPE);
        }

        @Override
        public Long getSize() {
            return attachment.getLong(BaseAttachmentService.CONTENT_LENGTH);
        }

        @Override
        public InputStream getStream() throws IOException {
            return attachmentContentDao.loadContent(attachment);
        }

        @Override
        public Reader getReader() throws IOException {
            return new InputStreamReader(getStream());
        }

    }

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        // Проверка включения агента индексирования
        if (configHelper.isDisableIndexing()){
            return;
        }

        List<SearchConfigHelper.SearchAreaDetailsConfig> configs =
                configHelper.findEffectiveConfigs(deletedDomainObject.getTypeName());
        if (configs.size() == 0) {
            return;
        }
        ArrayList<String> solrIds = new ArrayList<>(configs.size());
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            solrIds.add(createUniqueId(deletedDomainObject, config));
        }
        requestQueue.addRequest(new UpdateRequest().deleteById(solrIds));
        if (log.isInfoEnabled()) {
            log.info("" + solrIds.size() + " Solr document(s) queued for deleting");
        }
    }
}
