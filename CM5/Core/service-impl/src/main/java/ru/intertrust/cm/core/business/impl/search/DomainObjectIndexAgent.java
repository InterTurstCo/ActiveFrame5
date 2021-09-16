package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.search.CompoundFieldConfig;
import ru.intertrust.cm.core.config.search.CompoundFieldsConfig;
import ru.intertrust.cm.core.config.search.IndexedContentConfig;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.dao.api.extension.AfterDeleteAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveAfterCommitExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

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
    public void postInit() {
        super.init();
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        index(domainObject);
    }

    @Override
    public void index(DomainObject domainObject) {
        // Проверка включения агента индексирования
        if (validate(domainObject)) {
            return;
        }

        List<SearchConfigHelper.SearchAreaDetailsConfig> configs =
                configHelper.findEffectiveConfigs(domainObject.getTypeName());
        if (configs.size() == 0) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("DomainObjectIndexAgent::onAfterSave: domainObject.id=" +
                    (domainObject.getId() != null ? domainObject.getId().toString() : "null"));
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

            if (isNested(config)) {
                continue;
            }

            List<Id> mainIds = calculateMainObjects(domainObject.getId(), config.getObjectConfigChain());

            if (mainIds.size() > 0) {
                // обработка для нетиповвых полей
                reindexObjectAndChildren(solrDocs, domainObject, config, mainIds);
            } else {
                toDelete.add(createUniqueId(domainObject, config));
            }
        }

        addRequestIfNeeded(toDelete);
        addDocumentsIfNeeded(solrDocs);
    }

    private void addDocumentsIfNeeded(ArrayList<SolrInputDocument> solrDocs) {
        if (solrDocs.size() > 0) {
            // requestQueue.addDocuments(solrDocs);
            solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addDocuments(solrDocs);
            if (log.isInfoEnabled()) {
                log.info("" + solrDocs.size() + " Solr document(s) queued for indexing");
            }
        }
    }

    private boolean isNested(SearchConfigHelper.SearchAreaDetailsConfig config) {
        IndexedDomainObjectConfig objectConfig = config.getObjectConfig();
        if (objectConfig instanceof LinkedDomainObjectConfig) {
            return ((LinkedDomainObjectConfig) objectConfig).isNested();
        }
        return false;
    }

    private boolean validate(DomainObject deletedDomainObject) {
        if (configHelper.isDisableIndexing()) {
            return true;
        }
        return deletedDomainObject == null;
    }

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        // Проверка включения агента индексирования
        if (validate(deletedDomainObject)) {
            return;
        }

        List<SearchConfigHelper.SearchAreaDetailsConfig> configs =
                configHelper.findEffectiveConfigs(deletedDomainObject.getTypeName());
        if (configs.size() == 0) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("DomainObjectIndexAgent::onAfterDelete: deletedDomainObject.id=" +
                    (deletedDomainObject.getId() != null ? deletedDomainObject.getId().toString() : "null"));
        }

        ArrayList<String> solrIds = new ArrayList<>(configs.size());
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            if (isCntxSolrServer(config.getSolrServerKey())) {
                // Context search
                continue;
            }

            if (isNested(config)) {
                continue;
            }

            solrIds.add(createUniqueId(deletedDomainObject, config));

        }
        addRequestIfNeeded(solrIds);
    }

    private void addRequestIfNeeded(ArrayList<String> solrIds) {
        if (solrIds.size() > 0) {
            solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addRequest(new UpdateRequest().deleteById(solrIds));
            if (log.isInfoEnabled()) {
                log.info("" + solrIds.size() + " Solr document(s) queued for deleting");
                for (String delId : solrIds) {
                    log.info("toDelete.id=" + delId);
                }

            }
        }
    }

    private void reindexObjectAndChildren(List<SolrInputDocument> solrDocs, DomainObject object,
                                          SearchConfigHelper.SearchAreaDetailsConfig config, List<Id> mainIds) {
        for (Id mainId : mainIds) {
            SolrInputDocument doc = createSolsDocByDomainObject(object, mainId, config);
            solrDocs.add(doc);
        }

        for (SearchConfigHelper.SearchAreaDetailsConfig linkedConfig : configHelper.findChildConfigs(config, false)) {
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

    private void sendAttachment(DomainObject attachmentObject, SearchConfigHelper.SearchAreaDetailsConfig attachmentConfig) {
        // Проверка на то что данный тип вложения надо индексировать
        if (needIndex(attachmentObject)) {
            String linkName = configHelper.getAttachmentParentLinkName(attachmentObject.getTypeName(),
                    attachmentConfig.getObjectConfig().getType());
            List<Id> mainIds = calculateMainObjects(attachmentObject.getReference(linkName), attachmentConfig.getObjectConfigChain());
            Double boostValue = null;
            if (!mainIds.isEmpty()) {
                for (IndexedContentConfig contentConfig : attachmentConfig.getObjectConfig().getContentObjects()) {
                    // Проверка соответствия типа вложения и типа в конфигурации <indexed-content>
                    // на случай нескольких <indexed-content>
                    if (contentConfig.getType().equalsIgnoreCase(attachmentObject.getTypeName())) {
                        boostValue = contentConfig.getIndexBoostValue();
                        // если совпал тип вложения и тип <indexed-content>, то считаем, что дальше искать не нужно
                        break;
                    }
                }
            }

            for (Id mainId : mainIds) {
                ContentStreamUpdateRequest request = getRequestWithCommonParams(attachmentObject, attachmentConfig, mainId);
                addFieldToContentRequest(request, attachmentObject, BaseAttachmentService.NAME,
                        new TextSearchFieldType(configHelper.getSupportedLanguages()));
                addFieldToContentRequest(request, attachmentObject, BaseAttachmentService.DESCRIPTION,
                        new TextSearchFieldType(configHelper.getSupportedLanguages()));
                addFieldToContentRequest(request, attachmentObject, BaseAttachmentService.CONTENT_LENGTH,
                        new SimpleSearchFieldType(SimpleSearchFieldType.Type.LONG));
                request.setParam(SolrUtils.PARAM_FIELD_PREFIX + SolrUtils.ID_FIELD, createUniqueId(attachmentObject, attachmentConfig));
                request.setParam("uprefix", "cm_c_");
                request.setParam("fmap.content", SolrFields.CONTENT);
                // TODO сделать для нескольких полей контента
                if (boostValue != null) {
                    request.setParam("boost." + SolrFields.CONTENT, boostValue.toString());
                }
                //request.setParam("extractOnly", "true");
                solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addRequest(request);
            }
            if (log.isInfoEnabled()) {
                log.info("Attachment queued for indexing");
            }
        } else {
            log.debug("Indexing attachment " + attachmentObject.getString("name") + " is ignored by file extension");
        }
    }

    private SolrInputDocument createSolsDocByDomainObject(DomainObject domainObject,
                                                          Id mainId, SearchConfigHelper.SearchAreaDetailsConfig config) {
        SolrInputDocument doc = new SolrInputDocument();
        // System fields
        doc.addField(SolrFields.OBJECT_ID, domainObject.getId().toStringRepresentation());
        doc.addField(SolrFields.AREA, config.getAreaName());
        doc.addField(SolrFields.TARGET_TYPE, config.getTargetObjectType());
        doc.addField(SolrFields.OBJECT_TYPE, config.getObjectConfig().getType());
        doc.addField(SolrFields.MAIN_OBJECT_ID, mainId.toStringRepresentation());
        doc.addField(SolrFields.MODIFIED, domainObject.getModifiedDate());

        addBusinessFields(domainObject, config, doc);
        doc.addField("id", createUniqueId(domainObject, config));

        List<SearchConfigHelper.SearchAreaDetailsConfig> childConfigs = configHelper.findChildConfigs(config, true);
        addFields(doc, domainObject, childConfigs);

        return doc;
    }

    private void addBusinessFields(DomainObject domainObject, SearchConfigHelper.SearchAreaDetailsConfig config, SolrInputDocument doc) {
        addBusinessFields(domainObject, config, doc, null);
    }

    private void addBusinessFields(DomainObject domainObject, SearchConfigHelper.SearchAreaDetailsConfig config, SolrInputDocument doc, CompoundFieldCollector collector) {
        for (IndexedFieldConfig fieldConfig : config.getObjectConfig().getFields()) {
            CompoundFieldsConfig compoundFieldsConfig = fieldConfig.getCompoundFieldsConfig();
            if (compoundFieldsConfig != null && collector != null) {
                List<CompoundFieldConfig> fields = compoundFieldsConfig.getFieldPart();
                for (int index = 0; index < fields.size(); ++index) {
                    addBusinessFieldsByConfig(domainObject, fieldConfig, collector,
                            compoundFieldsConfig.getDelimiter(), index, fields.get(index));
                }
                return;
            }

            addBusinessFieldsByConfig(domainObject, doc, fieldConfig);
        }
    }

    private void addBusinessFieldsByConfig(DomainObject domainObject, SolrInputDocument doc, IndexedFieldConfig fieldConfig) {
        Double boostValue = fieldConfig.getIndexBoostValue();
        Map<SearchFieldType, ?> values = calculateField(domainObject, fieldConfig);
        for (Map.Entry<SearchFieldType, ?> entry : values.entrySet()) {
            SearchFieldType type = entry.getKey();
            for (String fieldName : type.getSolrFieldNames(fieldConfig.getName())) {
//                if (boostValue != null) {
//                    doc.addField(fieldName, entry.getValue(), boostValue.floatValue());
//                } else {
                    doc.addField(fieldName, entry.getValue());
//                }
            }
        }
    }

    private void addBusinessFieldsByConfig(DomainObject domainObject, IndexedFieldConfig fieldConfig,
                                           CompoundFieldCollector collector, String delimiter, int index,
                                           CompoundFieldConfig compoundFieldConfig) {
        Double boostValue = fieldConfig.getIndexBoostValue();
        Map<SearchFieldType, ?> values = calculateField(domainObject, fieldConfig, compoundFieldConfig);
        for (Map.Entry<SearchFieldType, ?> entry : values.entrySet()) {
            SearchFieldType type = entry.getKey();
            for (String fieldName : type.getSolrFieldNames(fieldConfig.getName())) {
                collector.addDelimiter(fieldName, delimiter);
                collector.addBoost(fieldName, boostValue);
                collector.add(fieldName, index, (String) entry.getValue());
            }
        }
    }

    private void addFields(SolrInputDocument doc, DomainObject domainObject, List<SearchConfigHelper.SearchAreaDetailsConfig> configs) {
        CompoundFieldCollector collector = new CompoundFieldCollector();
        for (SearchConfigHelper.SearchAreaDetailsConfig linkedConfig : configs) {
            for (DomainObject child : findChildren(domainObject.getId(), linkedConfig, true)) {
                addBusinessFields(child, linkedConfig, doc, collector);
            }
        }
        if (!collector.isEmpty()) {
            collector.collect().forEach(it -> {
                Double boost = it.getRight();
//                if (boost != null) {
//                    doc.addField(it.getLeft(), it.getMiddle(), boost.floatValue());
//                } else {
                    doc.addField(it.getLeft(), it.getMiddle());
//                }
            });
        }
    }

}
