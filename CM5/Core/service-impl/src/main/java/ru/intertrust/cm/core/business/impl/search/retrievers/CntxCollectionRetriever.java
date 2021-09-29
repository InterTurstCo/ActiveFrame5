package ru.intertrust.cm.core.business.impl.search.retrievers;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.business.impl.search.SolrFields;
import ru.intertrust.cm.core.business.impl.search.SolrUtils;
import ru.intertrust.cm.core.business.impl.search.TargetResultField;
import ru.intertrust.cm.core.config.*;

import java.math.BigDecimal;
import java.util.*;

@Service
@Scope("prototype")
public class CntxCollectionRetriever extends CollectionRetriever {
    private static final int MAX_IDS_PER_QUERY = 2000;

    private static Logger log = LoggerFactory.getLogger(CntxCollectionRetriever.class);

    @Autowired
    private CollectionsService collectionsService;

    private String collectionName;

    private String cntxFilterName;

    private Collection<TargetResultField> solrFields;

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setCntxFilterName(String cntxFilterName) {
        this.cntxFilterName = cntxFilterName;
    }

    public void setSolrFields(Collection<TargetResultField> solrFields) {
        this.solrFields = solrFields;
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
                    ArrayList<Filter> filters = new ArrayList<>(idsSize);
                    filters.add(createIdFilter(ids.subList(cnt, Math.min(cnt + MAX_IDS_PER_QUERY, idsSize))));
                    result.append(collectionsService.findCollection(collectionName,
                            new SortOrder(), filters, 0, Math.min(MAX_IDS_PER_QUERY, idsSize)));
                } else {
                    ArrayList<Filter> filters = new ArrayList<>(0);
                    result.append(collectionsService.findCollection(collectionName,
                            new SortOrder(), filters, 0, Math.min(MAX_IDS_PER_QUERY, idsSize)));
                }

            }
            addSolrFields(result, solrDocsMap, hl);
            addWeightsAndSort(result, found);
            truncCollection(result, maxResults);
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
                    List<FieldConfig> fields = collection.getFieldsConfiguration();
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
                        value = new BooleanValue(extractValue(objVal, Boolean.class));
                        break;
                    case DATETIME:
                        value = new DateTimeValue(extractValue(objVal, Date.class));
                        break;
                    case DECIMAL:
                        value = new DecimalValue(extractValue(objVal, BigDecimal.class));
                        break;
                    case LONG:
                        value = new LongValue(extractValue(objVal, Long.class));
                        break;
                    case STRING:
                        List<String> values = extractValues(objVal, String.class);
                        String strVal = null;
                        if (!values.isEmpty()) {
                            strVal = "";
                            for (String s : values) {
                                strVal += (strVal.isEmpty() ? "" : ", ") + s;
                            }
                        }
                        value = new StringValue(strVal);
                        break;
                    case REFERENCE:
                        String val = extractValue(objVal, String.class);
                        value = new ReferenceValue(val != null ? idService.createId(val) : null);
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

    private <T extends Object> T extractValue (Object rawValue,  Class<T> clazz) {
        T value = null;
        if (rawValue != null) {
            if (clazz.isInstance(rawValue)) {
                value = (T)rawValue;
            } else if (rawValue instanceof Collection) {
                Iterator iterator = ((Collection) rawValue).iterator();
                while (iterator.hasNext()) {
                    rawValue = iterator.next();
                    if (rawValue == null) {
                        continue;
                    }
                    if (clazz.isInstance(rawValue)) {
                        value = (T)rawValue;
                        break;
                    }
                }
            }
        }
        return value;
    }

    private <T extends Object> List<T> extractValues (Object rawValue,  Class<T> clazz) {
        List<T> list = new ArrayList<>(1);
        if (rawValue != null) {
            if (clazz.isInstance(rawValue)) {
                list.add((T)rawValue);
            } else if (rawValue instanceof Collection) {
                Iterator iterator = ((Collection) rawValue).iterator();
                while (iterator.hasNext()) {
                    rawValue = iterator.next();
                    if (rawValue == null) {
                        continue;
                    }
                    if (clazz.isInstance(rawValue)) {
                        list.add((T)rawValue);
                    }
                }
            }
        }
        return list;
    }
}
