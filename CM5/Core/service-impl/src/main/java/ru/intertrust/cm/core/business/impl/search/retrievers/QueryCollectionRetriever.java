package ru.intertrust.cm.core.business.impl.search.retrievers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.impl.search.SolrFields;

@Service
@Scope("prototype")
public class QueryCollectionRetriever extends CollectionRetriever {

    @Autowired
    private CollectionsService collectionsService;

    private String sqlQuery;
    private List<? extends Value<?>> sqlParameters;

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public void setSqlParameters(List<? extends Value<?>> sqlParameters) {
        this.sqlParameters = sqlParameters;
    }

    @Override
    public IdentifiableObjectCollection queryCollection(SolrDocumentList documents,
                                                        Map<String, Map<String, List<String>>> highlightings,
                                                        int maxResults) {
        throw new RuntimeException("Not implemented: " +
                "QueryCollectionRetriever.queryCollection(SolrDocumentList found, " +
                "Map<String, Map<String, List<String>>> highlightings, " +
                "int maxResults");
    }

    @Override
    public IdentifiableObjectCollection queryCollection(SolrDocumentList documents, int maxResults) {
        if (documents.isEmpty()) {
            return new GenericIdentifiableObjectCollection();
        }
        ArrayList<ReferenceValue> ids = new ArrayList<>();
        for (SolrDocument doc : documents) {
            Id id = idService.createId((String) doc.getFieldValue(SolrFields.MAIN_OBJECT_ID));
            ids.add(new ReferenceValue(id));
        }

        List<Value> modifiedParams = new ArrayList<>();
        int index = 0;
        if (sqlParameters != null){
            modifiedParams.addAll(sqlParameters);
            index = sqlParameters.size();
        }
        StringBuilder listQuery = new StringBuilder("(");
        boolean first = true;

        for (ReferenceValue refValue : ids) {
            modifiedParams.add(refValue);
            if (first) {
                first = false;
            } else {
                listQuery.append(", ");
            }
            listQuery.append("{").append(index).append("}");
            index++;
        }
        listQuery.append(")");
        String modifiedQuery = "select * from (" + sqlQuery + ") orig" + (ids.isEmpty() ? "" : (" where id in " + listQuery.toString()));

        IdentifiableObjectCollection result = collectionsService.
                findCollectionByQuery(modifiedQuery, modifiedParams, 0, Math.max(ids.size(), maxResults));

        addWeightsAndSort(result, documents);
        truncCollection(result, maxResults);
        return result;
    }

}
