package ru.intertrust.cm.core.business.impl.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.IdsIncludedFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortOrder;

public class NamedCollectionRetriever extends CollectionRetriever {

    public static final int MAX_IDS_PER_QUERY = 2000;

    private static Logger log = LoggerFactory.getLogger(NamedCollectionRetriever.class);

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

        IdentifiableObjectCollection result = null;
        if (idFilter.getCriterionKeys().size() > MAX_IDS_PER_QUERY) {
            //CMFIVE-5387 workaround: splitting query having too many IDs into smaller portions
            if (log.isDebugEnabled()) {
                log.debug("Too meany IDs requested (" + idFilter.getCriterionKeys().size()
                        + "), splitting DB queries by " + MAX_IDS_PER_QUERY + " IDs");
            }
            ArrayList<ReferenceValue> partIds = new ArrayList<>(MAX_IDS_PER_QUERY);
            for (int part = 0; part < (idFilter.getCriterionKeys().size() + MAX_IDS_PER_QUERY - 1) / MAX_IDS_PER_QUERY;
                    ++part) {
                int partSize = Math.min(MAX_IDS_PER_QUERY, idFilter.getCriterionKeys().size() - part * MAX_IDS_PER_QUERY);
                for (int i = 0; i < partSize; ++i) {
                    partIds.add(idFilter.getCriterion(i + part * MAX_IDS_PER_QUERY));
                }
                IdsIncludedFilter partialIdFilter = new IdsIncludedFilter(partIds);
                modifiedFilters.add(partialIdFilter);
                IdentifiableObjectCollection partialResult = collectionsService.findCollection(collectionName,
                        new SortOrder(), modifiedFilters, 0, maxResults);
                if (log.isDebugEnabled()) {
                    log.debug("Part " + part + ": " + partialResult.size() + " object(s) fetched");
                }
                if (result == null) {
                    result = partialResult;
                } else {
                    result.append(partialResult);
                }
                if (maxResults != 0) {      // maxResults==0 on call means unlimited result
                    maxResults -= partialResult.size();
                    if (maxResults <= 0) {
                        break;
                    }
                }
                // preparing for next iteration
                modifiedFilters.remove(modifiedFilters.size() - 1);
                partIds.clear();
            }
            //CMFIVE-5387 ----------
        } else {
            modifiedFilters.add(idFilter);
            result = collectionsService.findCollection(collectionName, new SortOrder(), modifiedFilters, 0, maxResults);
        }

        addWeightsAndSort(result, found);
        return result;
    }

    private IdsIncludedFilter intersectFilters(IdsIncludedFilter filter1, IdsIncludedFilter filter2) {
        if (log.isDebugEnabled()) {
            log.debug("Requested collection is already filtered by IDs, they will be intersected with found IDs");
        }
        HashSet<ReferenceValue> ids = new HashSet<>(filter1.getCriterionKeys().size());
        for (int i : filter1.getCriterionKeys()) {
            ids.add(filter1.getCriterion(i));
        }
        ArrayList<ReferenceValue> result = new ArrayList<>();
        for (int i : filter2.getCriterionKeys()) {
            ReferenceValue value = filter2.getCriterion(i);
            if (ids.contains(value)) {
                result.add(value);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("" + result.size() + " ID(s) included out of "
                    + filter2.getCriterionKeys().size() + " (collection defined) and "
                    + filter1.getCriterionKeys().size() + " (found) IDs");
        }
        return new IdsIncludedFilter(result);
    }

}
