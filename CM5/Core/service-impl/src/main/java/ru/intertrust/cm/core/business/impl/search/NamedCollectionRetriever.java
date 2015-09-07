package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortOrder;

public class NamedCollectionRetriever extends CollectionRetriever {

    @Autowired private IdService idService;
    @Autowired private CollectionsService collectionsService;

    private String collectionName;
    private List<? extends Filter> collectionFilters;

    public NamedCollectionRetriever(String collectionName) {
        this.collectionName = collectionName;
    }

    public NamedCollectionRetriever(String collectionName, List<? extends Filter> filters) {
        this.collectionName = collectionName;
        this.collectionFilters = filters;
    }

    @Override
    public IdentifiableObjectCollection queryCollection(SolrDocumentList found, int maxResults) {
        ArrayList<ReferenceValue> ids = new ArrayList<>();
        for (SolrDocument doc : found) {
            Id id = idService.createId((String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID));
            ids.add(new ReferenceValue(id));
        }
        IdsIncludedFilter idFilter = new IdsIncludedFilter(ids);
        ArrayList<Filter> modifiedFilters = new ArrayList<>();
        if (collectionFilters != null) {
            modifiedFilters.ensureCapacity(collectionFilters.size());
            for (Filter filter : collectionFilters) {
                if (filter instanceof IdsIncludedFilter) {
                    idFilter = intersectFilters(idFilter, (IdsIncludedFilter) filter);
                    // will be added to the end of the list
                } else {
                    modifiedFilters.add(filter);
                }
            }
        }
        modifiedFilters.add(idFilter);
        return collectionsService.findCollection(collectionName, new SortOrder(), modifiedFilters, 0, maxResults);
    }

    private IdsIncludedFilter intersectFilters(IdsIncludedFilter filter1, IdsIncludedFilter filter2) {
        HashSet<ReferenceValue> ids = new HashSet<>(filter1.getCriterionKeys().size());
        for (int i : filter1.getCriterionKeys()) {
            ids.add(filter1.getCriterion(i));
        }
        ArrayList<ReferenceValue> result = new ArrayList<>(Math.min(ids.size(), filter2.getCriterionKeys().size()));
        for (int i : filter2.getCriterionKeys()) {
            ReferenceValue value = filter2.getCriterion(i);
            if (ids.contains(value)) {
                result.add(value);
            }
        }
        return new IdsIncludedFilter(result);
    }

}
