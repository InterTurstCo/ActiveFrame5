package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.*;

import java.math.BigDecimal;
import java.util.*;

public class CntxCollectionRetriever extends CollectionRetriever {
    private static final int MAX_IDS_PER_QUERY = 2000;

    private static Logger log = LoggerFactory.getLogger(CntxCollectionRetriever.class);

    @Autowired
    private CollectionsService collectionsService;

    private String collectionName;

    private String cntxFilterName;

    private Collection<TargetResultField> solrFields;

    public CntxCollectionRetriever(String collectionName, String cntxFilterName, Collection<TargetResultField> solrFields) {
        this.collectionName = collectionName;
        this.solrFields = solrFields;
        this.cntxFilterName = cntxFilterName;
    }

    @Override
    public IdentifiableObjectCollection queryCollection(SolrDocumentList found, int maxResults) {
        return queryCollection(found, null, maxResults);
    }

    @Override
    public IdentifiableObjectCollection queryCollection(SolrDocumentList found,
                                                        Map<String, Map<String, List<String>>> highlightings,
                                                        int maxResults) {
        IdentifiableObjectCollection result = new GenericIdentifiableObjectCollection();
        if (!found.isEmpty()) {
            ArrayList<Value> ids = new ArrayList<>(found.size());
            Map<Id, Map<String, List<String>>> hl = new HashMap<>();
            Map<Id, SolrDocument> solrDocsMap = new HashMap<>();

            for (SolrDocument doc : found) {
                Id id = idService.createId((String) doc.getFieldValue(SolrFields.OBJECT_ID));
                ids.add(new ReferenceValue(id));
                Id mid = idService.createId((String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID));
                String solrId = (String) doc.getFieldValue(SolrUtils.ID_FIELD);
                solrDocsMap.put(mid, doc);
                if (highlightings != null) {
                    hl.put(mid, highlightings.get(solrId));
                }
            }
            int idsSize = ids.size();
            for (int cnt = 0; cnt < idsSize; cnt += MAX_IDS_PER_QUERY) {
                if (cntxFilterName != null) {
                    ArrayList<Filter> filters = new ArrayList<>(1);
                    filters.add(createIdFilter(ids.subList(cnt, Math.min(cnt + MAX_IDS_PER_QUERY, idsSize))));
                    result.append(collectionsService.findCollection(collectionName, new SortOrder(), filters, 0, maxResults));
                } else {
                    ArrayList<Filter> filters = new ArrayList<>(0);
                    result.append(collectionsService.findCollection(collectionName, new SortOrder(), filters, cnt, maxResults));
                }

            }
            addSolrFields(result, solrDocsMap, hl);
            addWeightsAndSort(result, found);
        }
        return result;
    }

    private Filter createIdFilter(List<Value> ids) {
        Filter idFilter = new Filter();
        idFilter.setFilter(cntxFilterName);
        idFilter.addMultiCriterion(0, ids);
        return idFilter;
    }

    private void addSolrFields(IdentifiableObjectCollection collection,
                               Map<Id, SolrDocument> solrDocs,
                               Map<Id, Map<String, List<String>>> highlightings) {
        if (collection == null || collection.size() == 0) {
            return;
        }
        if (solrFields != null && !solrFields.isEmpty()) {
            for (TargetResultField solrField : solrFields) {
                FieldConfig fieldConfig = createFieldConfig(solrField);
                if (fieldConfig != null) {
                    ArrayList<FieldConfig> fields = collection.getFieldsConfiguration();
                    if (!fields.contains(fieldConfig)) {
                        fields.add(fieldConfig);
                        collection.setFieldsConfiguration(fields);
                    }
                }
            }
            for (int i = 0; i < collection.size(); i++) {
                fillFieldValues(i, collection, solrFields, solrDocs, highlightings);
            }
        }
    }

    private void fillFieldValues(int rowIdx,
                                 IdentifiableObjectCollection collection,
                                 Collection<TargetResultField> solrFields,
                                 Map<Id, SolrDocument> solrDocs,
                                 Map<Id, Map<String, List<String>>> highlightings) {
        Id id = collection.getId(rowIdx);
        for (TargetResultField solrField : solrFields) {
            int colIdx = collection.getFieldIndex(solrField.getResultFieldName());
            if (colIdx >= 0) {
                FieldType fieldType = solrField.getDataFieldType();
                if (solrField.isHighlighting() && fieldType == FieldType.LIST) {
                    List<StringValue> valueList = composeHighlighting(id, solrField, highlightings.get(id));
                    ListValue data = new ListValue();
                    if (!valueList.isEmpty()) {
                        StringValue[] array = new StringValue[valueList.size()];
                        data = new ListValue(valueList.toArray(array));
                    }
                    collection.set(colIdx, rowIdx, data);
                }  else {
                    Value value = composeFieldValue(id, solrField, solrDocs.get(id));
                    if (value != null) {
                        collection.set(colIdx, rowIdx, value);
                    }
                }
            }
        }
    }

    private FieldConfig createFieldConfig(TargetResultField resultField) {
        FieldConfig fieldConfig = null;
        if (resultField != null) {
            String name = resultField.getResultFieldName();
            FieldType fieldType = resultField.getDataFieldType();

            switch (fieldType) {
                case BOOLEAN:
                    fieldConfig = new BooleanFieldConfig();
                    break;
                case DATETIME:
                    fieldConfig = new DateTimeFieldConfig();
                    break;
                case STRING:
                    fieldConfig = new StringFieldConfig();
                    break;
                case DECIMAL:
                    fieldConfig = new DecimalFieldConfig();
                    break;
                case LONG:
                    fieldConfig = new LongFieldConfig();
                    break;
                case REFERENCE:
                    fieldConfig = new ReferenceFieldConfig();
                    break;
                case LIST:
                    fieldConfig = new FieldConfig() {
                        @Override
                        public FieldType getFieldType() {
                            return FieldType.LIST;
                        }
                    };
                    break;
                default:
                    break;
            }
            if (fieldConfig != null) {
                fieldConfig.setName(name);
            }
        }
        return fieldConfig;
    }

    private List<StringValue> composeHighlighting(Id id,
                                                  TargetResultField resultField,
                                                  Map<String, List<String>> highlighting) {
        List<StringValue> result = null;
        if (highlighting != null) {
            result = new ArrayList<>();
            for (String fieldName : resultField.getSolrFieldNames()) {
                List<String> hlList = highlighting.get(fieldName);
                if (hlList != null && !hlList.isEmpty()) {
                    for (String hl : hlList) {
                        if (hl != null && !hl.trim().isEmpty()) {
                            result.add(new StringValue(hl));
                        }
                    }
                }
            }
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    private Value composeFieldValue(Id id,
                                    TargetResultField resultField,
                                    SolrDocument solrDoc) {
        Value value = null;
        if (solrDoc != null && resultField != null) {
            for (String fieldName : resultField.getSolrFieldNames()) {
                Object objVal = solrDoc.getFieldValue(fieldName);
                switch (resultField.getDataFieldType()) {
                    case BOOLEAN:
                        value = new BooleanValue((objVal instanceof Boolean) ? (Boolean) objVal : null);
                        break;
                    case DATETIME:
                        value = new DateTimeValue((objVal instanceof Date) ? (Date) objVal : null);
                        break;
                    case DECIMAL:
                        value = new DecimalValue((objVal instanceof BigDecimal) ? (BigDecimal) objVal : null);
                        break;
                    case LONG:
                        value = new LongValue((objVal instanceof Long) ? (Long) objVal : null);
                        break;
                    case STRING:
                        value = new StringValue((objVal instanceof String) ? (String) objVal : null);
                        break;
                    case REFERENCE:
                        value = new ReferenceValue(objVal != null ? idService.createId(objVal.toString()) : null);
                        break;
                    default:
                        break;
                }

                if (objVal != null && !objVal.toString().isEmpty()) {
                    break;
                }
            }
        }
        return value;
    }
}
