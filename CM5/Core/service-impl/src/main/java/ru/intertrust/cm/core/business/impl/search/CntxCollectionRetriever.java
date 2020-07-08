package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;

import java.util.*;

public class CntxCollectionRetriever extends CollectionRetriever {
    private static final int MAX_IDS_PER_QUERY = 2000;
    private static final String SNIPPET_FIELD_NAME = "highlighting";
    private static final String CNTX_FILTER = "CNTX_ID_FILTER";
    private static final FieldConfig SNIPPET_FIELD = new StringFieldConfig();
    static {
        SNIPPET_FIELD.setName(SNIPPET_FIELD_NAME);
    }

    private static Logger log = LoggerFactory.getLogger(CntxCollectionRetriever.class);

    @Autowired
    private CollectionsService collectionsService;

    private String collectionName;

    public CntxCollectionRetriever(String collectionName) {
        this.collectionName = collectionName;
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
            for (SolrDocument doc : found) {
                Id id = idService.createId((String) doc.getFieldValue(SolrFields.OBJECT_ID));
                ids.add(new ReferenceValue(id));
            }
            int idsSize = ids.size();
            for (int cnt = 0; cnt < idsSize; cnt += MAX_IDS_PER_QUERY) {
                ArrayList<Filter> filters = new ArrayList<>(1);
                filters.add(createIdFilter(ids.subList(cnt, Math.min(cnt + MAX_IDS_PER_QUERY, idsSize))));
                result.append(collectionsService.findCollection(collectionName, new SortOrder(), filters, 0, maxResults));
            }
            addHilighting(result, found, highlightings);
            addWeightsAndSort(result, found);
        }
        return result;
    }

    private Filter createIdFilter(List<Value> ids) {
        Filter idFilter = new Filter();
        idFilter.setFilter(CNTX_FILTER);
        idFilter.addMultiCriterion(0, ids);
        return idFilter;
    }

    private void addHilighting(IdentifiableObjectCollection collection,
                               SolrDocumentList solrDocs,
                               Map<String, Map<String, List<String>>> highlightings) {
        if (collection == null || collection.size() == 0) {
            return;
        }
        Map<Id, String> hlIds = new HashMap<>();
        for (SolrDocument solrDoc : solrDocs) {
            Id id = idService.createId((String) solrDoc.getFieldValue(SolrFields.MAIN_OBJECT_ID));
            String hlId = (String) solrDoc.getFieldValue(SolrUtils.ID_FIELD);
            hlIds.put(id, hlId);
        }

        ArrayList<FieldConfig> fields = collection.getFieldsConfiguration();
        if (!fields.contains(SNIPPET_FIELD)) {
            fields.add(SNIPPET_FIELD);
            collection.setFieldsConfiguration(fields);
        }
        int snippetIdx = collection.getFieldIndex(SNIPPET_FIELD_NAME);

        for (int i = 0; i < collection.size(); ++i) {
            Id id = collection.getId(i);
            collection.set(snippetIdx, i, new StringValue(composeHilighting(highlightings, hlIds.get(id))));
        }
    }

    private String composeHilighting(Map<String, Map<String, List<String>>> highlightings, String id) {
        String hl = "";
        if (highlightings != null) {
            Map<String, List<String>> hlValues = highlightings.get(id);
            if (hlValues != null) {
                for (Map.Entry<String, List<String>> entry : hlValues.entrySet()) {
                    if (entry.getKey() != null && entry.getKey().toLowerCase().contains((SolrFields.CONTENT.toLowerCase()))) {
                        List<String> hlList = entry.getValue() != null ? entry.getValue() : null;
                        if (hlList != null && !hlList.isEmpty()) {
                            for (String hlVal : hlList) {
                                hl += (hlVal != null ? hlVal : "") + (!hl.isEmpty() ? " ... " : "");
                            }
                            if (!hl.isEmpty()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return hl;
    }

}
