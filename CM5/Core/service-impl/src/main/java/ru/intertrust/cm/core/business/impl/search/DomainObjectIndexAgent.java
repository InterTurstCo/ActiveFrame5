package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import ru.intertrust.cm.core.business.api.BaseAttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;
import ru.intertrust.cm.core.config.search.*;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.extension.*;

import javax.annotation.PostConstruct;
import java.util.*;

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
        implements AfterSaveAfterCommitExtensionHandler, AfterDeleteAfterCommitExtensionHandler,
        BeforeDeleteExtensionHandler {

    private static final Set<String> sysFields = new HashSet<>(Arrays.asList(
        "id",
        SolrFields.OBJECT_ID,
        SolrFields.AREA,
        SolrFields.TARGET_TYPE,
        SolrFields.OBJECT_TYPE,
        SolrFields.MAIN_OBJECT_ID,
        SolrFields.MODIFIED
        ));

    private static final ObjectCache<String, List<Id>> cache = new ObjectCache<>();

    @PostConstruct
    public void postInit() {
        super.init();
    }

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        // Проверка включения агента индексирования
        if (configHelper.isDisableIndexing()) {
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
            log.info("DomainObjectIndexAgent::onAfterSave: domainObject.id=" +
                    (domainObject.getId() != null ? domainObject.getId().toString() : "null"));
        }

        ArrayList<SolrInputDocument> solrDocs = new ArrayList<>(configs.size());
        ArrayList<String> toDelete = new ArrayList<>();

        String cmjField = domainObject.getString("cmjfield");

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
                if (cmjField != null && cmjField.startsWith("Tn$_")) {
                    // отдельная обработка для нетиповвых объектов
                    reindexTnObject(solrDocs, toDelete, domainObject, config, mainIds, domainObject.getTypeName());
                } else {
                    // отдельная обработка для нетиповвых полей
                    reindexObjectAndChildren(solrDocs, domainObject, config, mainIds);
                }
            } else {
                toDelete.add(createUniqueId(domainObject, config));
            }
        }
        if (toDelete.size() > 0) {
            // requestQueue.addRequest(new UpdateRequest().deleteById(toDelete));
            solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addRequest(new UpdateRequest().deleteById(toDelete));
            if (log.isInfoEnabled()) {
                log.info("" + toDelete.size() + " Solr document(s) queued for deleting");
                for (String delId : toDelete) {
                    log.info("toDelete.id=" + delId);
                }
            }
        }
        if (solrDocs.size() > 0) {
            // requestQueue.addDocuments(solrDocs);
            solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addDocuments(solrDocs);
            if (log.isInfoEnabled()) {
                log.info("" + solrDocs.size() + " Solr document(s) queued for indexing");
            }
        }
    }

    @Override
    public void onBeforeDelete(DomainObject deletedDomainObject) {
        // Проверка включения агента индексирования
        if (configHelper.isDisableIndexing()) {
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
            log.info("DomainObjectIndexAgent::onBeforeDelete: deletedDomainObject.id=" +
                    (deletedDomainObject.getId() != null ? deletedDomainObject.getId().toString() : "null"));
        }

        String cmjField = deletedDomainObject.getString("cmjfield");

        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            if (isCntxSolrServer(config.getSolrServerKey())) {
                // Context search
                continue;
            }
            if (configHelper.isAttachmentObject(deletedDomainObject)) {
                continue;
            }

            if (cmjField != null && cmjField.startsWith("Tn$_")) {
                // отдельная обработка для нетиповвых объектов
                List<Id> mainIds = calculateMainObjects(deletedDomainObject.getId(), config.getObjectConfigChain());
                if (!mainIds.isEmpty()) {
                    cache.put(generateCacheKey(deletedDomainObject.getId(), config.getAreaName()), mainIds);
                }
            }
        }
    }

    @Override
    public void onAfterDelete(DomainObject deletedDomainObject) {
        // Проверка включения агента индексирования
        if (configHelper.isDisableIndexing()) {
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
            log.info("DomainObjectIndexAgent::onAfterDelete: deletedDomainObject.id=" +
                    (deletedDomainObject.getId() != null ? deletedDomainObject.getId().toString() : "null"));
        }

        String cmjField = deletedDomainObject.getString("cmjfield");

        ArrayList<SolrInputDocument> solrDocs = new ArrayList<>(configs.size());
        ArrayList<String> solrIds = new ArrayList<>(configs.size());
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            if (isCntxSolrServer(config.getSolrServerKey())) {
                // Context search
                continue;
            }

            solrIds.add(createUniqueId(deletedDomainObject, config));

            if (cmjField != null && cmjField.startsWith("Tn$_")) {
                // отдельная обработка для нетиповвых объектов
                List<Id> mainIds = cache.fetchAndRemove(generateCacheKey(deletedDomainObject.getId(), config.getAreaName()));
                if (mainIds != null && !mainIds.isEmpty()) {
                    reindexTnObject(solrDocs, solrIds, deletedDomainObject, config, mainIds, deletedDomainObject.getTypeName());
                }
            }
        }
        if (solrIds.size() > 0) {
            solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addRequest(new UpdateRequest().deleteById(solrIds));
            if (log.isInfoEnabled()) {
                log.info("" + solrIds.size() + " Solr document(s) queued for deleting");
                for (String delId : solrIds) {
                    log.info("toDelete.id=" + delId);
                }

            }
        }
        if (solrDocs.size() > 0) {
            solrServerWrapperMap.getRegularSolrServerWrapper().getQueue().addDocuments(solrDocs);
            if (log.isInfoEnabled()) {
                log.info("" + solrDocs.size() + " Solr document(s) queued for indexing");
            }
        }
    }

    private void reindexObjectAndChildren(List<SolrInputDocument> solrDocs, DomainObject object,
                                          SearchConfigHelper.SearchAreaDetailsConfig config, List<Id> mainIds) {
        for (Id mainId : mainIds) {
            SolrInputDocument doc = createSolsDocByDomainObject(object, mainId, config);
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
            Double boostValue = null;
            if (mainIds != null && !mainIds.isEmpty()) {
                for (IndexedContentConfig contentConfig : config.getObjectConfig().getContentObjects()) {
                    // Проверка соответствия типа вложения и типа в конфигурации <indexed-content>
                    // на случай нескольких <indexed-content>
                    if (contentConfig.getType().equalsIgnoreCase(object.getTypeName())) {
                        boostValue = contentConfig.getIndexBoostValue();
                        // если совпал тип вложения и тип <indexed-content>, то считаем, что дальше искать не нужно
                        break;
                    }
                }
            }
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
            log.debug("Indexing attachment " + object.getString("name") + " is ignored by file extension");
        }
    }

    private void reindexTnObject(List<SolrInputDocument> solrDocs, ArrayList<String> toDelete,
                                 DomainObject object, SearchConfigHelper.SearchAreaDetailsConfig config,
                                 List<Id> mainIds, String tnType) {
        List<SolrInputDocument> tmpSolrDocs = new ArrayList<>();
        List<String> tmpTodelete = new ArrayList<>();
        String areaName = config.getAreaName();
        if (areaName == null) {
            return;
        }
        AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
        for (Id mainId : mainIds) {
            // SolrInputDocument doc = createSolsDocByDomainObject(object, mainId, config);
            // tmpSolrDocs.add(doc);
            // по mainId находим все дочерние объекты данного типа
            // Выбираем главный объект
            DomainObject mainDomainObject = object != null && object.getId().equals(mainId) ?
                    object : domainObjectDao.find(mainId, accessToken);
            if (mainDomainObject == null) {
                continue;
            }
            // Находим конфигурацию
            List<SearchConfigHelper.SearchAreaDetailsConfig> mainConfigs =
                    configHelper.findEffectiveConfigs(mainDomainObject.getTypeName());
            if (mainConfigs.size() == 0) {
                return;
            }
            for (SearchConfigHelper.SearchAreaDetailsConfig mainConfig : mainConfigs) {
                if (areaName.equalsIgnoreCase(mainConfig.getAreaName()) && !isCntxSolrServer(mainConfig.getSolrServerKey())) {
                    for (SearchConfigHelper.SearchAreaDetailsConfig linkedConfig : configHelper.findChildConfigs(mainConfig)) {
                        String linkedType = linkedConfig.getObjectConfig().getType();
                        if (!tnType.equalsIgnoreCase(linkedType)) {
                            continue;
                        }
                        for (DomainObject child : findChildren(mainId, linkedConfig, tnType)) {
                            if (tnType.equalsIgnoreCase(child.getTypeName())) {
                                List<SearchConfigHelper.SearchAreaDetailsConfig> configs = configHelper.findEffectiveConfigs(child.getTypeName());
                                for (SearchConfigHelper.SearchAreaDetailsConfig cfg : configs) {
                                    if (!areaName.equalsIgnoreCase(cfg.getAreaName())) {
                                        // другая область поиска, пропускаем
                                        continue;
                                    }
                                    if (isCntxSolrServer(cfg.getSolrServerKey())) {
                                        // Context search
                                        continue;
                                    }
                                    if (configHelper.isAttachmentObject(child)) {
                                        // Если это вложение, то пропускаем
                                        continue;
                                    } else {
                                        // если это объект, то дополняем solr-документ полями дочерних объектов
                                        SolrInputDocument doc = createSolsDocByDomainObject(child, mainId, cfg);
                                        tmpSolrDocs.add(doc);
                                        tmpTodelete.add(createUniqueId(child, cfg));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        solrDocs.addAll(compactSolrDocList(tmpSolrDocs));
        toDelete.addAll(tmpTodelete);
    }

    private List<SolrInputDocument> compactSolrDocList(List<SolrInputDocument> srcSolrDocs) {
        List<SolrInputDocument> solrDocs = new ArrayList<>();
        if (srcSolrDocs != null) {
            solrDocs.addAll(srcSolrDocs);
            // перебираем список документов и уплотняем следующим образом:
            // если в документах списки непустых полей не пересекаются или значения непустых полей совпадают,
            // то сливаем такие документы в один. В противном случае копируем непустие поля в пустие с соответствующими именами
            Iterator<SolrInputDocument> iterator = solrDocs.iterator();
            // Удалаем пустые документы
            while (iterator.hasNext()) {
                SolrInputDocument solrDoc = iterator.next();
                if (isSolrDocEmpty(solrDoc)) {
                    iterator.remove();
                }
            }
            // копируем непустые поля
            for (int idx1 = 0; idx1 < solrDocs.size(); idx1++) {
                for (int idx2 = idx1 + 1 ; idx2 < solrDocs.size(); idx2++) {
                    copyNonEmptyFields(solrDocs.get(idx1), solrDocs.get(idx2));
                }
            }
            // удаляем совпадающие
            for (int idx1 = 0; idx1 < solrDocs.size(); idx1++) {
                for (int idx2 = solrDocs.size() - 1; idx2 > idx1; idx2--) {
                    if (isSolrDocsEqual(solrDocs.get(idx1), solrDocs.get(idx2))) {
                        solrDocs.remove(idx2);
                    }
                }
            }
        }
        return solrDocs;
    }

    private boolean isSolrDocEmpty(SolrInputDocument solrDoc) {
        boolean isEmpty = true;
        Collection<String> fields = new ArrayList<>(solrDoc.getFieldNames());
        for (String field : fields) {
            if (!sysFields.contains(field)) {
                Object value = solrDoc.getFieldValue(field);
                isEmpty &= value == null;
                if (!isEmpty) {
                    break;
                }
            }
        }
        return isEmpty;
    }

    private boolean isSolrDocsEqual(SolrInputDocument solrDoc1, SolrInputDocument solrDoc2) {
        boolean isEqual = true;
        Collection<String> fields1 = new ArrayList<>(solrDoc1.getFieldNames());
        Collection<String> fields2 = new ArrayList<>(solrDoc2.getFieldNames());
        for (String field : fields1) {
            if (fields2.contains(field)) {
                fields2.remove(field);
            }
            if (!sysFields.contains(field)) {
                Object value1 = solrDoc1.getFieldValue(field);
                Object value2 = solrDoc2.getFieldValue(field);
                isEqual &= (value1 != null ? value1.equals(value2) : value2 == null);
                if (!isEqual) {
                    break;
                }
            }
        }
        if (isEqual) {
            for (String field : fields2) {
                if (!sysFields.contains(field)) {
                    Object value1 = solrDoc1.getFieldValue(field);
                    Object value2 = solrDoc2.getFieldValue(field);
                    isEqual &= (value1 != null ? value1.equals(value2) : value2 == null);
                    if (!isEqual) {
                        break;
                    }
                }
            }
        }
        return isEqual;
    }

    private void copyNonEmptyFields(SolrInputDocument solrDoc1, SolrInputDocument solrDoc2) {
        Collection<String> fields1 = new ArrayList<>(solrDoc1.getFieldNames());
        Collection<String> fields2 = new ArrayList<>(solrDoc2.getFieldNames());
        for (String field : fields1) {
            if (fields2.contains(field)) {
                fields2.remove(field);
            }
            if (!sysFields.contains(field)) {
                // Object value1 = solrDoc1.getFieldValue(field);
                // Object value2 = solrDoc2.getFieldValue(field);
                SolrInputField solrInputField1 = solrDoc1.getField(field);
                SolrInputField solrInputField2 = solrDoc2.getField(field);
                Object value1 = solrInputField1 != null ? solrInputField1.getValue() : null;
                Object value2 = solrInputField2 != null ? solrInputField2.getValue() : null;
                if (value1 != null && value2 == null) {
                    solrDoc2.addField(field, value1, solrInputField1.getBoost());
                }
                if (value1 == null && value2 != null) {
                    solrDoc1.addField(field, value2, solrInputField2.getBoost());
                }
            }
        }
        for (String field : fields2) {
            if (!sysFields.contains(field)) {
                // Object value1 = solrDoc1.getFieldValue(field);
                // Object value2 = solrDoc2.getFieldValue(field);
                SolrInputField solrInputField1 = solrDoc1.getField(field);
                SolrInputField solrInputField2 = solrDoc2.getField(field);
                Object value1 = solrInputField1 != null ? solrInputField1.getValue() : null;
                Object value2 = solrInputField2 != null ? solrInputField2.getValue() : null;
                if (value1 != null && value2 == null) {
                    solrDoc2.addField(field, value1, solrInputField1.getBoost());
                }
                if (value1 == null && value2 != null) {
                    solrDoc1.addField(field, value2, solrInputField2.getBoost());
                }
            }
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

        // Business fields
        for (IndexedFieldConfig fieldConfig : config.getObjectConfig().getFields()) {
            Double boostValue = fieldConfig.getIndexBoostValue();
            Map<SearchFieldType, ?> values = calculateField(domainObject, fieldConfig);
            for (Map.Entry<SearchFieldType, ?> entry : values.entrySet()) {
                SearchFieldType type = entry.getKey();
                for (String fieldName : type.getSolrFieldNames(fieldConfig.getName())) {
                    if (boostValue != null) {
                        doc.addField(fieldName, entry.getValue(), boostValue.floatValue());
                    } else {
                        doc.addField(fieldName, entry.getValue());
                    }
                }
            }
        }
        doc.addField("id", createUniqueId(domainObject, config));
        return doc;
    }

    private String generateCacheKey(Id id, String areaName) {
        return (id != null ? id.toStringRepresentation() : "null")  + ":" + (areaName != null ? areaName : "null");
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
