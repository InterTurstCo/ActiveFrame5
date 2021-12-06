package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.search.ContentFieldConfig;
import ru.intertrust.cm.core.config.search.IndexedContentConfig;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.BeforeDeleteExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

import javax.annotation.PostConstruct;
import java.util.*;

@ExtensionPoint
public class DomainObjectCntxIndexAgent extends DomainObjectIndexAgentBase
        implements AfterSaveAfterCommitExtensionHandler, AfterDeleteAfterCommitExtensionHandler, BeforeDeleteExtensionHandler {

    private static final ObjectCache<String, CachedSolrDocs> cache = new ObjectCache<>();


    enum IndexingAction {
        ADD("add"),
        UPDATE("set"),
        DELETE("remove");

        private String text;
        private IndexingAction(String text) {
            this.text = text;
        }
        public String getText() {
            return text;
        }
    }

    @PostConstruct
    public void postInit(){
        super.init();
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        index(domainObject);
    }

    @Override
    public void index(DomainObject domainObject) {
        // Проверка включения агента индексирования
        if (configHelper.isDisableIndexing()){
            return;
        }

        if (domainObject == null) {
            return;
        }

        List<SearchConfigHelper.SearchAreaDetailsConfig> configs =
                configHelper.findEffectiveConfigs(domainObject.getTypeName());
        if (configs.size() == 0) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("DomainObjectCntxIndexAgent::onAfterSave: domainObject.id=" +
                    (domainObject.getId() != null ? domainObject.getId().toString() : "null"));
        }

        Map<String, List<SolrInputDocument>> solrDocs = new HashMap<>();
        Map<String, List<String>> solrIds = new HashMap<>();
        // Map<String, List<String>> toDelete = new HashMap<>();
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            if (!isCntxSolrServer(config.getSolrServerKey())) {
                // Regular search
                continue;
            }
            if (configHelper.isAttachmentObject(domainObject)) {
                // Если это вложение, то формируем запрос
                List<Id> mainIds = indexAttachment(solrDocs, domainObject, config, config.getSolrServerKey());
                String cntxMode = solrSearchConfiguration.getSolrCntxMode();
                if (SolrUtils.CNTX_MODE_SMART.equalsIgnoreCase(cntxMode)) {
                    if (mainIds != null && !mainIds.isEmpty()) {
                        for (Id mId : mainIds) {
                            if (mId != null) {
                                addSolrDocIdToDelete(solrIds, config.getSolrServerKey(), createUniqueId(mId.toStringRepresentation(), config));
                            }
                        }
                    }
                }
                // continue;
            } else {
                // если это сам объект, то ищем вложения и переиндексмируем
                List<Id> mainIds = calculateMainObjects(domainObject.getId(), config.getObjectConfigChain());
                if (!mainIds.isEmpty()) {
                    Map<String, SolrInputDocument> solrDocMap = collectSolrDocs( domainObject, config, mainIds, IndexingAction.UPDATE);
                    for (SolrInputDocument solrDoc : solrDocMap.values()) {
                        addSolrInputDoc(solrDocs, config.getSolrServerKey(), solrDoc);
                    }
                }
            }
        }
        putSolrDocsToQueue(solrDocs);
        putSolrDocIdsToQueue(solrIds);
    }

    @Override
    public void onBeforeDelete(DomainObject deletedDomainObject) {
        // Проверка включения агента индексирования
        if (configHelper.isDisableIndexing()){
            return;
        }

        if (deletedDomainObject == null) {
            return;
        }

        List<SearchConfigHelper.SearchAreaDetailsConfig> configs =
                configHelper.findEffectiveConfigs(deletedDomainObject.getTypeName());
        if (configs.size() == 0) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("DomainObjectCntxIndexAgent::onBeforeDelete: deletedDomainObject.id=" +
                    (deletedDomainObject.getId() != null ? deletedDomainObject.getId().toString() : "null"));
        }

        Map<String, List<SolrInputDocument>> solrDocs = new HashMap<>();
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            if (!isCntxSolrServer(config.getSolrServerKey())) {
                // Regular search
                continue;
            }
            if (configHelper.isAttachmentObject(deletedDomainObject)) {
                continue;
            } else {
                // если это сам объект, то ищем вложения и переиндексмируем
                List<Id> mainIds = calculateMainObjects(deletedDomainObject.getId(), config.getObjectConfigChain());
                if (!mainIds.isEmpty()) {
                    Map<String, SolrInputDocument> solrDocMap = collectSolrDocs(deletedDomainObject, config, mainIds, IndexingAction.DELETE);
                    cache.put(generateCacheKey(deletedDomainObject.getId(), config.getAreaName()), new CachedSolrDocs(mainIds, solrDocMap));
                }
            }
        }
    }

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        // Проверка включения агента индексирования
        if (configHelper.isDisableIndexing()){
            return;
        }

        if (deletedDomainObject == null) {
            return;
        }

        List<SearchConfigHelper.SearchAreaDetailsConfig> configs =
                configHelper.findEffectiveConfigs(deletedDomainObject.getTypeName());
        if (configs.size() == 0) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("DomainObjectCntxIndexAgent::onAfterDelete: deletedDomainObject.id=" +
                    (deletedDomainObject.getId() != null ? deletedDomainObject.getId().toString() : "null"));
        }

        Map<String, List<SolrInputDocument>> solrDocs = new HashMap<>();
        Map<String, List<String>> solrIds = new HashMap<>();
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            if (!isCntxSolrServer(config.getSolrServerKey())) {
                // Regular search
                continue;
            }

            if (configHelper.isAttachmentObject(deletedDomainObject)) {
                // Если это вложение, то формируем запрос
                addSolrDocIdToDelete(solrIds, config.getSolrServerKey(), createUniqueId(deletedDomainObject, config));
            }
            else {
                // если это сам объект, то ищем вложения и переиндексмируем
                CachedSolrDocs docsToDelete = cache.fetchAndRemove(generateCacheKey(deletedDomainObject.getId(), config.getAreaName()));
                if (docsToDelete != null && docsToDelete.getSolrDocs() != null) {
                    //Map<String, SolrInputDocument> solrDocMap =
                    // collectSolrDocs(deletedDomainObject, config, docsToDelete.getMainIds(), IndexingAction.UPDATE);
                    for (SolrInputDocument solrDoc : docsToDelete.getSolrDocs().values()) {
                        addSolrInputDoc(solrDocs, config.getSolrServerKey(), solrDoc);
                    }
                }
            }
        }

        putSolrDocsToQueue(solrDocs);
        putSolrDocIdsToQueue(solrIds);
    }

    private List<Id> indexAttachment(Map<String, List<SolrInputDocument>> solrDocs,
                                 DomainObject attachmentObject,
                                 SearchConfigHelper.SearchAreaDetailsConfig attachmentConfig,
                                 String solrServerKey) {
        List<Id> mainIds = null;
        // Проверка на то что данный тип вложения надо индексировать
        if (needIndex(attachmentObject)) {
            String linkName = configHelper.getAttachmentParentLinkName(attachmentObject.getTypeName(),
                    attachmentConfig.getObjectConfig().getType());
            mainIds = calculateMainObjects(attachmentObject.getReference(linkName), attachmentConfig.getObjectConfigChain());
            Map<String, ContentFieldConfig> contentFieldConfigs = new HashMap<>();
            Double boostValue = null;
            if (mainIds != null && !mainIds.isEmpty()) {
                for (IndexedContentConfig contentConfig : attachmentConfig.getObjectConfig().getContentObjects()) {
                    // Проверка соответствия типа вложения и типа в конфигурации <indexed-content>
                    // на случай нескольких <indexed-content>
                    if (contentConfig.getType().equalsIgnoreCase(attachmentObject.getTypeName())) {
                        for (ContentFieldConfig contentFieldConfig : contentConfig.getFields()) {
                            contentFieldConfigs.put(contentFieldConfig.getType().getFieldType(), contentFieldConfig);
                        }
                        boostValue = contentConfig.getIndexBoostValue();
                        // если совпал тип вложения и тип <indexed-content>, то считаем, что дальше искать не нужно
                        break;
                    }
                }
            }
            for (Id mainId : mainIds) {
                ContentStreamUpdateRequest request = getRequestWithCommonParams(attachmentObject, attachmentConfig, mainId);

                for (ContentFieldConfig contentFieldConfig : contentFieldConfigs.values()) {
                    switch (contentFieldConfig.getType()) {
                        case NAME :
                            addFieldToContentRequest(request, attachmentObject, BaseAttachmentService.NAME,
                                    contentFieldConfig.getType().getSolrFieldName(),
                                    new TextSearchFieldType(configHelper.getSupportedLanguages()));
                            break;
                        case PATH :
                            addFieldToContentRequest(request, attachmentObject, BaseAttachmentService.PATH,
                                    contentFieldConfig.getType().getSolrFieldName(),
                                    new TextSearchFieldType(configHelper.getSupportedLanguages()));
                            break;
                        case MIMETYPE :
                            addFieldToContentRequest(request, attachmentObject, BaseAttachmentService.MIME_TYPE,
                                    contentFieldConfig.getType().getSolrFieldName(),
                                    new TextSearchFieldType(configHelper.getSupportedLanguages()));
                            break;
                        case DESCRIPTION :
                            addFieldToContentRequest(request, attachmentObject, BaseAttachmentService.DESCRIPTION,
                                    contentFieldConfig.getType().getSolrFieldName(),
                                    new TextSearchFieldType(configHelper.getSupportedLanguages()));
                            break;
                        case LENGTH :
                            addFieldToContentRequest(request, attachmentObject, BaseAttachmentService.CONTENT_LENGTH,
                                    contentFieldConfig.getType().getSolrFieldName(),
                                    new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG));
                            break;
                        case REFID:
                            // SolrUtils.PARAM_FIELD_PREFIX + SolrFields.OBJECT_ID
                            break;
                        default:
                            break;
                    }
                }

                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrUtils.ID_FIELD, createUniqueId(attachmentObject, attachmentConfig));
                request.setParam("uprefix", "cm_c_");
                request.setParam("fmap.content", SolrFields.CONTENT);
                if (boostValue != null) {
                    request.setParam("boost." + SolrFields.CONTENT, boostValue.toString());
                }
                solrServerWrapperMap.getSolrServerWrapper(solrServerKey).getQueue().addRequest(request);

                // Добавляем поля родительского документа
                AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
                DomainObject mainDomainObject = domainObjectDao.find(mainId, accessToken);
                addSolrInputDoc(solrDocs, solrServerKey, getSolrDocToIndex(mainDomainObject, attachmentObject, attachmentConfig, IndexingAction.UPDATE));
            }
            if (log.isInfoEnabled()) {
                log.info("Attachment queued for indexing");
            }
        } else {
            log.debug("Indexing attachment " + attachmentObject.getString("name") + " is ignored by file extension");
        }
        return mainIds;
    }

    private SolrInputDocument getSolrDocToIndex(DomainObject mainDomainObject,
                                                DomainObject attachmentObject,
                                                SearchConfigHelper.SearchAreaDetailsConfig objectConfig,
                                                IndexingAction action) {
        SolrInputDocument solrDoc = null;
        if (mainDomainObject != null) {
            solrDoc = new SolrInputDocument();
            solrDoc.addField(SolrUtils.ID_FIELD, createUniqueId(attachmentObject != null ? attachmentObject : mainDomainObject, objectConfig));
            solrDoc.addField(SolrFields.OBJECT_ID, attachmentObject != null ?
                    attachmentObject.getId().toStringRepresentation() : mainDomainObject.getId().toStringRepresentation());
            solrDoc.addField(SolrFields.AREA, objectConfig.getAreaName());
            solrDoc.addField(SolrFields.TARGET_TYPE, objectConfig.getTargetObjectType());
            solrDoc.addField(SolrFields.OBJECT_TYPE, objectConfig.getObjectConfig().getType());
            solrDoc.addField(SolrFields.MAIN_OBJECT_ID, mainDomainObject.getId().toStringRepresentation());
            solrDoc.addField(SolrFields.MODIFIED, ThreadSafeDateFormat.format(mainDomainObject.getModifiedDate(), DATE_PATTERN));
            solrDoc.addField(SolrUtils.ATTACH_FLAG_FIELD, attachmentObject != null);
            if (action != IndexingAction.DELETE) {
                updateSolrDoc(solrDoc, mainDomainObject, objectConfig, IndexingAction.UPDATE);
                // перебор всех дочерних объектов и заполение полей solr-документа
                for (SearchConfigHelper.SearchAreaDetailsConfig linkedConfig : configHelper.findChildConfigs(objectConfig)) {
                    for (DomainObject child : findChildren(mainDomainObject.getId(), linkedConfig)) {
                        List<SearchConfigHelper.SearchAreaDetailsConfig> configs = configHelper.findEffectiveConfigs(child.getTypeName());
                        for (SearchConfigHelper.SearchAreaDetailsConfig cfg : configs) {
                            if (!objectConfig.getAreaName().equalsIgnoreCase(cfg.getAreaName())) {
                                // другая область поиска, пропускаем
                                continue;
                            }
                            if (!isCntxSolrServer(cfg.getSolrServerKey())) {
                                // Regular search
                                continue;
                            }
                            if (configHelper.isAttachmentObject(child)) {
                                // Если это вложение, то пропускаем
                                continue;
                            } else {
                                // если это объект, то дополняем solr-документ полями дочерних объектов
                                Set<Id> setIds = new HashSet<>(calculateMainObjects(child.getId(), cfg.getObjectConfigChain()));
                                if (setIds.contains(mainDomainObject.getId())) {
                                    updateSolrDoc(solrDoc, child, cfg, IndexingAction.UPDATE);
                                }
                            }
                        }
                    }
                }
            }
        }
        return solrDoc;
    }

    private Map<String, SolrInputDocument> collectSolrDocs(DomainObject object,
                                                           SearchConfigHelper.SearchAreaDetailsConfig objectConfig,
                                                           List<Id> mainIds,
                                                           IndexingAction action) {
        Map<String, SolrInputDocument> solrDocMap = new LinkedHashMap<>();
        AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
        for (Id mainId : mainIds) {
            DomainObject mainDomainObject = object != null && object.getId().equals(mainId) ?
                    object : domainObjectDao.find(mainId, accessToken);
            if (mainDomainObject == null) {
                continue;
            }
            SolrInputDocument solrDoc = null;

            //  индексируем документ с каждым вложением
            boolean isAttach = false;
            Set<IndexedContentConfig> contentConfigs = new LinkedHashSet<>();
            IndexedDomainObjectConfig[] objectConfigs = objectConfig.getObjectConfigChain();
            for (IndexedDomainObjectConfig cfg : objectConfigs) {
                contentConfigs.addAll(cfg.getContentObjects());
            }
            for (IndexedContentConfig contentConfig : contentConfigs) {
                if (contentConfig.getParentFkField() == null) {
                    continue;
                }
                List<DomainObject> attachments = domainObjectDao.findLinkedDomainObjects(mainId,
                        contentConfig.getType(), contentConfig.getParentFkField(), accessToken);
                if (attachments != null && !attachments.isEmpty()) {
                    for (DomainObject attachment : attachments) {
                        List<SearchConfigHelper.SearchAreaDetailsConfig> attachmentConfigs =
                                configHelper.findEffectiveConfigs(attachment.getTypeName());
                        for (SearchConfigHelper.SearchAreaDetailsConfig attachmentConfig : attachmentConfigs) {
                            if (!objectConfig.getAreaName().equalsIgnoreCase(attachmentConfig.getAreaName())) {
                                continue;
                            }
                            solrDoc = getSolrDocToIndex(mainDomainObject, attachment, objectConfig, action);
                            if (solrDoc != null && action == IndexingAction.DELETE) {
                                updateSolrDoc(solrDoc, object, objectConfig, IndexingAction.DELETE);
                            }
                            solrDocMap.put(solrDoc.get(SolrUtils.ID_FIELD).toString(), solrDoc);
                            isAttach = true;
                        }
                    }
                }
            }
            if (!isAttach) {
                // индексируем документ без вложений
                String cntxMode = solrSearchConfiguration.getSolrCntxMode();
                if (SolrUtils.CNTX_MODE_SMART.equalsIgnoreCase(cntxMode)) {
                    solrDoc = getSolrDocToIndex(mainDomainObject, null, objectConfig, action);
                    if (solrDoc != null && action == IndexingAction.DELETE) {
                        updateSolrDoc(solrDoc, object, objectConfig, IndexingAction.DELETE);
                    }
                    solrDocMap.put(solrDoc.get(SolrUtils.ID_FIELD).toString(), solrDoc);
                }
            }
        }
        return solrDocMap;
    }

    private void updateSolrDoc(SolrInputDocument solrDoc, DomainObject object,
                               SearchConfigHelper.SearchAreaDetailsConfig config, IndexingAction action) {
        if (object != null && solrDoc != null) {
            // Business fields
            for (IndexedFieldConfig fieldConfig : config.getObjectConfig().getFields()) {
                Double boostValue = fieldConfig.getIndexBoostValue();
                Map<SearchFieldType, ?> values = calculateField(object, fieldConfig);
                for (Map.Entry<SearchFieldType, ?> entry : values.entrySet()) {
                    SearchFieldType type = entry.getKey();
                    for (String fieldName : type.getSolrFieldNames(fieldConfig.getName())) {
                        Map<String, Object> fieldModifier = new HashMap<>(1);
                        fieldModifier.put(action == IndexingAction.ADD && !fieldConfig.getMultiValued() ?
                                IndexingAction.UPDATE.getText() : action.getText(), entry.getValue());
//                        if (boostValue != null) {
//                            solrDoc.addField(fieldName, fieldModifier, boostValue.floatValue());
//                        } else {
                            solrDoc.addField(fieldName, fieldModifier);
//                        }
                    }
                }
            }
        }
    }

    private String generateCacheKey(Id id, String areaName) {
        return (id != null ? id.toStringRepresentation() : "null")  + ":" + (areaName != null ? areaName : "null");
    }

    private void putSolrDocsToQueue (Map<String, List<SolrInputDocument>> solrDocs) {
        if (solrDocs != null &&solrDocs.size() > 0) {
            int cnt = 0;
            for (Map.Entry<String, List<SolrInputDocument>> entry : solrDocs.entrySet()) {
                List<SolrInputDocument> list = entry.getValue();
                if (list != null && !list.isEmpty()) {
                    solrServerWrapperMap.getSolrServerWrapper(entry.getKey()).getQueue().addDocuments(list);
                    cnt += list.size();
                }
            }
            if (log.isInfoEnabled()) {
                log.info("" + cnt + " Solr document(s) queued for indexing");
            }
        }
    }

    private void putSolrDocIdsToQueue (Map<String, List<String>> solrIds) {
        if (solrIds.size() > 0) {
            int cnt = 0;
            for (Map.Entry<String, List<String>> entry : solrIds.entrySet()) {
                List<String> list = entry.getValue();
                if (list != null && !list.isEmpty()) {
                    solrServerWrapperMap.getSolrServerWrapper(entry.getKey()).getQueue().addRequest(new UpdateRequest().deleteById(list));
                    cnt += list.size();
                }
            }
            if (log.isInfoEnabled()) {
                log.info("" + cnt + " Solr document(s) queued for deleting");
            }
        }
    }

    private void addSolrInputDoc(Map<String, List<SolrInputDocument>> solrDocs, String key, SolrInputDocument solrDoc) {
        if (solrDocs != null && solrDoc != null) {
            if (!solrDocs.containsKey(key)) {
                solrDocs.put(key, new ArrayList<>());
            }
            solrDocs.get(key).add(solrDoc);
        }
    }

    private void addSolrDocIdToDelete(Map<String, List<String>> solrDocIds, String key, String solrDocId) {
        if (solrDocIds != null && solrDocId != null) {
            if (!solrDocIds.containsKey(key)) {
                solrDocIds.put(key, new ArrayList<>());
            }
            solrDocIds.get(key).add(solrDocId);
        }
    }

    private static class CachedSolrDocs {
        private final List<Id> mainIds;
        private final Map<String, SolrInputDocument> solrDocs;

        public CachedSolrDocs(List<Id> mainIds, Map<String, SolrInputDocument> solrDocs) {
            this.mainIds = mainIds;
            this.solrDocs = solrDocs;
        }

        public List<Id> getMainIds() {
            return mainIds;
        }

        public Map<String, SolrInputDocument> getSolrDocs() {
            return solrDocs;
        }
    }
}
