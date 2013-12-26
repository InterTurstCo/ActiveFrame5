package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint
public class DomainObjectIndexAgent implements AfterSaveExtensionHandler {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SolrServer solrServer;

    @Autowired
    private SearchConfigHelper configHelper;

    @Autowired
    private DoelEvaluator doelEvaluator;

    @Autowired
    private AccessControlService accessControlService;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        List<SearchConfigHelper.SearchAreaDetailsConfig> configs = configHelper.findEffectiveConfigs(domainObject);
        if (configs.size() == 0) {
            return;
        }
        ArrayList<SolrInputDocument> solrDocs = new ArrayList<>(configs.size());
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField(SolrFields.OBJECT_ID, domainObject.getId().toStringRepresentation());
            doc.addField(SolrFields.AREA, config.getAreaName());
            doc.addField(SolrFields.TARGET_TYPE, config.getTargetObjectType());
            for (IndexedFieldConfig fieldConfig : config.getObjectConfig().getFields()) {
                Object value = calculateField(domainObject, fieldConfig);
                StringBuilder fieldName = new StringBuilder()
                        .append(SolrFields.FIELD_PREFIX)
                        .append(fieldConfig.getName().toLowerCase())
                        .append(configHelper.getFieldType(fieldConfig, config.getTargetObjectType()).getSuffix());
                doc.addField(fieldName.toString(), value);
            }
            doc.addField("id", createUniqueId(domainObject, config));
            solrDocs.add(doc);
        }
        try {
            @SuppressWarnings("unused")
            UpdateResponse response = solrServer.add(solrDocs);
            solrServer.commit();
        } catch (Exception e) {
            log.error("Error indexing document " + domainObject.getId(), e);
            return;
        }
        if (log.isInfoEnabled()) {
            log.info(Integer.toString(solrDocs.size()) + " Solr document(s) added to index");
        }
    }

    private Object calculateField(DomainObject object, IndexedFieldConfig config) {
        if (config.getDoel() != null) {
            AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
            List<? extends Value> values = doelEvaluator.evaluate(
                    DoelExpression.parse(config.getDoel()), object.getId(), accessToken);
            if (values.size() == 0) {
                return null;
            } else if (values.size() == 1) {
                return convertValue(values.get(0));
            } else {
                ArrayList<Object> result = new ArrayList<>(values.size());
                for (Value value : values) {
                    result.add(convertValue(value));
                }
                return result;
            }
        }
        Value value = object.getValue(config.getName());
        return convertValue(value);
    }

    private Object convertValue(Value value) {
        if (value == null) {
            return null;
        }
        Object result;
        if (value instanceof ReferenceValue) {
            Id id = ((ReferenceValue) value).get();
            result = (id == null) ? null : id.toStringRepresentation();
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
}
