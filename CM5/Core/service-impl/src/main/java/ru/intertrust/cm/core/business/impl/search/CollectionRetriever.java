package ru.intertrust.cm.core.business.impl.search;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
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

    protected CollectionRetriever() {
        SpringApplicationContext.getContext().getAutowireCapableBeanFactory().autowireBean(this);
    }

    public abstract IdentifiableObjectCollection queryCollection(SolrDocumentList documents,
                                                                 Map<String, Map<String, List<String>>> hilightings,
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
