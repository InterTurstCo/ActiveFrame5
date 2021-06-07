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
import ru.intertrust.cm.core.business.api.dto.*;
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
import ru.intertrust.cm.core.model.ObjectNotFoundException;
import ru.intertrust.cm.core.tools.SearchAreaFilterScriptContext;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public abstract class DomainObjectIndexAgentBase {

    protected static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    protected Logger log = LoggerFactory.getLogger(DomainObjectIndexAgentBase.class);

    @Autowired
    protected SolrServerWrapperMap solrServerWrapperMap;

    @Autowired
    protected SearchConfigHelper configHelper;

    @Autowired
    protected DoelEvaluator doelEvaluator;

    @Autowired
    protected DomainObjectDao domainObjectDao;
    
    @Autowired
    protected AccessControlService accessControlService;
    
    @Autowired
    protected ScriptService scriptService;

    @Autowired
    protected AttachmentContentDao attachmentContentDao;

    @org.springframework.beans.factory.annotation.Value("${attachment.index.exclusion:avi,asf,mpg,mpeg,mpe,vob,mp4,m4v,3gp,3gpp,flv,swf,mov,divx,webm,wav,wma,mp3,ogg,aac,ac3,jpg,jpeg,bmp}")
    private String attachmentIndexExclusionConfig;

    private Set<String> exclusionSet = new HashSet<>();

    // @PostConstruct
    protected void init(){
        if (attachmentIndexExclusionConfig != null){
            String[] attachmentIndexExclusionConfigArray = attachmentIndexExclusionConfig.split("[,;\t]");
            for (String ignoreExtension : attachmentIndexExclusionConfigArray) {
                exclusionSet.add(ignoreExtension.toLowerCase().trim());
            }
        }
    }

    protected List<DomainObject> findChildren(Id objectId, SearchConfigHelper.SearchAreaDetailsConfig config) {
        return findChildren(objectId, config, null);
    }

    protected List<DomainObject> findChildren(Id objectId, SearchConfigHelper.SearchAreaDetailsConfig config, String objectType) {
        String objectTypeFromConfig = config.getObjectConfig().getType();
        if (objectType != null && !objectType.equalsIgnoreCase(objectTypeFromConfig)) {
            return Collections.emptyList();
        }
        ParentLinkConfig parentLinkConfig = ((LinkedDomainObjectConfig) config.getObjectConfig()).getParentLink();
        String parentLink = parentLinkConfig.getDoel();
        DoelExpression parentExpr = DoelExpression.parse(parentLink);
        DoelExpression linkedExpr;
        try {
            linkedExpr = doelEvaluator.createReverseExpression(parentExpr, objectTypeFromConfig,
                    objectType != null, parentLinkConfig.getTypesAsArray());
        } catch (DoelException e) {
            log.warn("Can't calculate children of type " + objectTypeFromConfig + ": " + e.getMessage()
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

    protected boolean needIndex(DomainObject object) {
        String name = object.getString(BaseAttachmentService.NAME);
        // Не индексируем вложения без имени
        if (name == null){
            return false;
        }
        // Проверка расширение файла на исключения из индексирования
        String extension = FilenameUtils.getExtension(name).toLowerCase();
        return !exclusionSet.contains(extension);
    }

    protected void addFieldToContentRequest(ContentStreamUpdateRequest request,
            DomainObject object, String fieldName, SearchFieldType fieldType) {
        addFieldToContentRequest(request, object, fieldName, fieldName, fieldType);
    }

    protected void addFieldToContentRequest(ContentStreamUpdateRequest request, DomainObject object,
                                            String fieldName, String solrFieldName,
                                            SearchFieldType fieldType) {
        Object value = convertValue(object.getValue(fieldName));
        if (value != null) {
            for (String solrField : fieldType.getSolrFieldNames(solrFieldName)) {
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + solrField, value.toString());
            }
        }
    }

    protected Map<SearchFieldType, ?> calculateField(DomainObject object, IndexedFieldConfig config) {
        try {
            Collection<SearchFieldType> types = configHelper.getFieldTypes(config, object.getTypeName());
            if (types.size() == 0) {
                return Collections.emptyMap();
            }

            if (config.getScriptConfig() != null) {
                SearchAreaFilterScriptContext context = new SearchAreaFilterScriptContext(object);
                Object value = scriptService.eval(config.getScriptConfig().getScript(), context);
                return Collections.singletonMap(types.iterator().next(), convertScriptValue(value));

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
                                List<Object> list = (List<Object>) result.computeIfAbsent(type, (key) -> new ArrayList<>());
                                list.add(convertValue(value));
                            }
                        } else if (type instanceof SimpleSearchFieldType && ((SimpleSearchFieldType) type).type == typeId) {
                            if (!((SimpleSearchFieldType) type).multiValued) {
                                result.put(type, convertValue(value));
                            } else {
                                @SuppressWarnings("unchecked")
                                List<Object> list = (List<Object>) result.computeIfAbsent(type, (key) -> new ArrayList<>());
                                list.add(convertValue(value));
                            }
                        } else if (type instanceof CustomSearchFieldType) {
                            @SuppressWarnings("unchecked")
                            List<Object> list = (List<Object>) result.computeIfAbsent(type, (key) -> new ArrayList<>());
                            list.add(convertValue(value));
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
            if (config.getScriptConfig() != null) {
                message.append(" [script=").append(config.getScriptConfig().getScript()).append("]");
            }
            if (config.getDoel() != null) {
                message.append(" [doel=").append(config.getDoel()).append("]");
            }
            log.error(message.toString(), e);
            return Collections.emptyMap();
        }
    }

    protected List<Id> calculateMainObjects(Id objectId, IndexedDomainObjectConfig[] configChain) {
        ArrayList<Id> ids = new ArrayList<>();
        if (objectId != null) {
            ids.add(objectId);
            if (configChain != null) {
                AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
                for (IndexedDomainObjectConfig config : configChain) {
                    if (config == null) {
                        continue;
                    }
                    ParentLinkConfig parentConfig = null;
                    if (LinkedDomainObjectConfig.class.isAssignableFrom(config.getClass())) {
                        parentConfig = ((LinkedDomainObjectConfig) config).getParentLink();
                    }
                    ArrayList<ReferenceValue> refs = new ArrayList<>();
                    for (Iterator<Id> itr = ids.iterator(); itr.hasNext(); ) {
                        Id id = itr.next();
                        if (id == null) {
                            itr.remove();
                            continue;
                        }
                        try {
                            DomainObject object = domainObjectDao.find(id, accessToken);
                            if (object != null) {
                                if (!configHelper.isSuitableType(config.getType(), object.getTypeName())) {
                                    itr.remove();
                                    continue;
                                }
                                DomainObjectFilter filter = configHelper.createFilter(config);
                                if (filter != null && !filter.filter(object)) {
                                    itr.remove();
                                    continue;
                                }
                            } else {
                                itr.remove();
                                continue;
                            }
                        } catch (ObjectNotFoundException e) {
                            itr.remove();
                            log.error("ObjectNotFoundException: " + e.getMessage());
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
                        if (ref != null && ref.get() != null) {
                            ids.add(ref.get());
                        }
                    }
                }
            }
        }
        return ids;
    }

    protected Object convertValue(Value<?> value) {
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


    protected Object convertScriptValue(Object value) {
        if (value == null) {
            return null;
        }
        Object result = null;
        if (value instanceof TimelessDate) {
            TimelessDate date = (TimelessDate) value;
            if (date != null) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.clear();
                cal.set(date.getYear(), date.getMonth(), date.getDayOfMonth());
                result = cal.getTime();
            }
        } else if (value instanceof DateTimeWithTimeZone) {
            DateTimeWithTimeZone date = (DateTimeWithTimeZone) value;
            if (date != null) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(date.getTimeZoneContext().getTimeZoneId()));
                cal.set(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                        date.getHours(), date.getMinutes(), date.getSeconds());
                result = cal.getTime();
            }
        } else if (value instanceof Date) {
            Date date = (Date) value;
            if (date != null) {
                result = date;
            }
        } else {
            result = value;
        }
        return result;
    }

    protected String createUniqueId(DomainObject object, SearchConfigHelper.SearchAreaDetailsConfig config) {
        return createUniqueId(object.getId().toStringRepresentation(), config);
    }

    protected String createUniqueId(String strId, SearchConfigHelper.SearchAreaDetailsConfig config) {
        StringBuilder buf = new StringBuilder();
        buf.append(strId)
                .append(":").append(config.getAreaName())
                .append(":").append(config.getTargetObjectType());
        return buf.toString();
    }

    protected boolean isCntxSolrServer(String key) {
        return solrServerWrapperMap.isCntxSolrServer(key);
    }

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

    protected static class ObjectCache<K, T> {
        private static final int cacheSize = 1000;
        private static final long delta = 10*60*1000;
        private ConcurrentMap<K, CacheValue<T>> cache = new ConcurrentHashMap<>();

        private static class CacheValue<T> {
            private final long modified;
            private final T object;

            private CacheValue(long modified, T object) {
                this.modified = modified;
                this.object = object;
            }

            public T getObject() {
                return object;
            }
        }

        public void put(K key, T object) {
            cache.put(key, new CacheValue(new Date().getTime(), object));
            int cnt = 5;
            long deltaLocal = delta;
            while (cache.size() > cacheSize && cnt > 0) {
                removeOld(deltaLocal);
                deltaLocal /= 10;
                cnt--;
            }
        }

        public T fetchAndRemove(K key) {
            CacheValue<T> value = cache.remove(key);
            return value != null ? value.getObject() : null;
        }

        private void removeOld(long delta) {
            Iterator<Map.Entry<K, CacheValue<T>>> iterator = cache.entrySet().iterator();
            long border = new Date().getTime() - delta;
            while (iterator.hasNext()) {
                Map.Entry<K, CacheValue<T>> entry = iterator.next();
                if (entry.getValue().modified < border) {
                    iterator.remove();
                }
            }
        }
    }

}
