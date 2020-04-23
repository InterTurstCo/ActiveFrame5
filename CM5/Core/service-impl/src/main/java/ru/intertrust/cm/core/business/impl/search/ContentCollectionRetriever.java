package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import ru.intertrust.cm.core.business.api.dto.GenericIdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;

import java.util.HashSet;

public class ContentCollectionRetriever extends CollectionRetriever {
    @Override
    public IdentifiableObjectCollection queryCollection(SolrDocumentList documents, int maxResults) {
        HashSet<Id> ids = new HashSet<>();
        for (SolrDocument doc : documents) {
            ids.add(idService.createId((String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID)));
        }

        GenericIdentifiableObjectCollection result = new GenericIdentifiableObjectCollection();
        int fetchStart = 0;
        int fetchSize = maxResults;
        int fieldsCount = 0;
        // TODO цикл по результатам поиска и фильтрация по ACL
        /*
        while (true) {
            IdentifiableObjectCollection sample = new sqlParameters == null
                    ? collectionsService.findCollectionByQuery(sqlQuery, fetchStart, fetchSize)
                    : collectionsService.findCollectionByQuery(sqlQuery, sqlParameters, fetchStart, fetchSize);
            if (fetchStart == 0) {
                result.setFieldsConfiguration(sample.getFieldsConfiguration());
                fieldsCount = sample.getFieldsConfiguration().size();
            }
            for (int row = 0; row < sample.size(); ++row) {
                if (ids.contains(sample.getId(row))) {
                    int destRow = result.size();
                    result.setId(destRow, sample.getId(row));
                    for (int i = 0; i < fieldsCount; ++i) {
                        result.set(i, destRow, sample.get(i, row));
                    }
                    if (result.size() == maxResults) {
                        addWeightsAndSort(result, documents);
                        return result;
                    }
                }
            }
            if (sample.size() < fetchSize) {
                break;
            }
            fetchStart += fetchSize;
            fetchSize = estimateFetchSize(fetchSize, result.size(), maxResults);
        }
       */
        addWeightsAndSort(result, documents);
        return result;
    }

    private int estimateFetchSize(int prevFetchSize, int collectedCount, int requiredSize) {
        if (collectedCount == 0) {
            ++collectedCount;
        }
        double factor = (double) requiredSize / collectedCount;
        return (int) Math.round(Math.ceil(prevFetchSize * factor));
    }

}
