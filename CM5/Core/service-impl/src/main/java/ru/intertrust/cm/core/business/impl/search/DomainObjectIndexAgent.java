package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class DomainObjectIndexAgent extends DomainObjectIndexAgentBase
        implements AfterSaveAfterCommitExtensionHandler, AfterDeleteAfterCommitExtensionHandler {

    @PostConstruct
    public void postInit(){
        super.init();
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
            if (isCntxSolrServer(config.getSolrServerKey())) {
                // Context search
                continue;
            }

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
            // requestQueue.addDocuments(solrDocs);
            solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addDocuments(solrDocs);
            if (log.isInfoEnabled()) {
                log.info("" + solrDocs.size() + " Solr document(s) queued for indexing");
            }
        }
        if (toDelete.size() > 0) {
            // requestQueue.addRequest(new UpdateRequest().deleteById(toDelete));
            solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addRequest(new UpdateRequest().deleteById(toDelete));
            if (log.isInfoEnabled()) {
                log.info("" + toDelete.size() + " Solr document(s) queued for deleting");
            }
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
            if (isCntxSolrServer(config.getSolrServerKey())) {
                // Context search
                continue;
            }
            solrIds.add(createUniqueId(deletedDomainObject, config));
        }
        solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addRequest(new UpdateRequest().deleteById(solrIds));
        if (log.isInfoEnabled()) {
            log.info("" + solrIds.size() + " Solr document(s) queued for deleting");
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
                    for (String fieldName : type.getSolrFieldNames(fieldConfig.getName())) {
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
                        new TextSearchFieldType(configHelper.getSupportedLanguages()));
                addFieldToContentRequest(request, object, BaseAttachmentService.DESCRIPTION,
                        new TextSearchFieldType(configHelper.getSupportedLanguages()));
                addFieldToContentRequest(request, object, BaseAttachmentService.CONTENT_LENGTH,
                        new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG));
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrUtils.ID_FIELD, createUniqueId(object, config));
                request.setParam("uprefix", "cm_c_");
                request.setParam("fmap.content", SolrFields.CONTENT);
                //request.setParam("extractOnly", "true");

                solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addRequest(request);
            }
            if (log.isInfoEnabled()) {
                log.info("Attachment queued for indexing");
            }
        }else{
            log.debug("Indexing attachment " + object.getString("name") + " is ignored by file extension");
        }
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
}
