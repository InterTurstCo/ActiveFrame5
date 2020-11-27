package ru.intertrust.cm.core.business.impl.search;

import java.math.BigDecimal;
import java.util.*;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.DecimalFieldConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.util.SpringApplicationContext;

public abstract class CollectionRetriever {

    @Autowired protected IdService idService;

    /*private*/ static final FieldConfig RELEVANCE_FIELD = new DecimalFieldConfig();
    /*private*/ static final SortOrder RELEVANCE_SORT = new SortOrder();
    static {
        RELEVANCE_FIELD.setName(SearchQuery.RELEVANCE);
        RELEVANCE_SORT.add(new SortCriterion(SearchQuery.RELEVANCE, SortCriterion.Order.DESCENDING));
    }

    public static void sortByRelevance(IdentifiableObjectCollection collection) {
        if (collection != null) {
            collection.sort(RELEVANCE_SORT);
        }
    }

    public static void truncCollection(IdentifiableObjectCollection collection, int maxResults) {
        // cокращение до maxResult
        if (collection != null && collection.size() > maxResults) {
            int cnt = 0;
            Iterator<IdentifiableObject> iterator = collection.iterator();
            while (iterator.hasNext()) {
                iterator.next();
                if (cnt >= maxResults) {
                    iterator.remove();
                } else {
                    cnt ++;
                }
            }
        }
    }

    protected CollectionRetriever() {
        SpringApplicationContext.getContext().getAutowireCapableBeanFactory().autowireBean(this);
    }

    public abstract IdentifiableObjectCollection queryCollection(SolrDocumentList documents,
                                                                 Map<String, Map<String, List<String>>> highlightings,
                                                                 int maxResults);

    public abstract IdentifiableObjectCollection queryCollection(SolrDocumentList documents,
                                                                 int maxResults);

    protected void addWeightsAndSort(IdentifiableObjectCollection objects, SolrDocumentList solrDocs) {
        Map<Id, Float> weights = new HashMap<Id, Float>();
        for (SolrDocument solrDoc : solrDocs) {
            Id id = idService.createId((String) solrDoc.getFieldValue(SolrFields.MAIN_OBJECT_ID));
            Float weight = (Float) solrDoc.getFieldValue(SolrUtils.SCORE_FIELD);
            weights.put(id, weight);
        }

        ArrayList<FieldConfig> fields = objects.getFieldsConfiguration();
        fields.add(RELEVANCE_FIELD);
        objects.setFieldsConfiguration(fields);
        int relevanceIdx = objects.getFieldIndex(SearchQuery.RELEVANCE);

        for (int i = 0; i < objects.size(); ++i) {
            Float weight = weights.get(objects.getId(i));
            objects.set(relevanceIdx, i, new DecimalValue(new BigDecimal(weight)));
        }

        objects.sort(RELEVANCE_SORT);
    }
}
