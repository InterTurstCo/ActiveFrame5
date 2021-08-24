package ru.intertrust.cm.core.business.impl.search.retrievers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.impl.search.TargetResultField;

import java.util.Collection;
import java.util.List;

@Service
public class CollectionRetrieverFactory {

    @Autowired
    private PureRetrieversFactory retrieversFactory;

    public CntxCollectionRetriever newCntxCollectionRetriever(String collectionName,
                                                              String cntxFilterName,
                                                              Collection<TargetResultField> solrFields) {
        CntxCollectionRetriever cntxCollectionRetriever = retrieversFactory.getCntxCollectionRetriever();
        cntxCollectionRetriever.setCollectionName(collectionName);
        cntxCollectionRetriever.setCntxFilterName(cntxFilterName);
        cntxCollectionRetriever.setSolrFields(solrFields);

        return cntxCollectionRetriever;
    }

    public NamedCollectionRetriever newNamedCollectionRetriever(String collectionName) {
        NamedCollectionRetriever namedCollectionRetriever = retrieversFactory.getNamedCollectionRetriever();
        namedCollectionRetriever.setCollectionName(collectionName);

        return namedCollectionRetriever;
    }

    public NamedCollectionRetriever newNamedCollectionRetriever(String collectionName, List<? extends Filter> filters) {
        NamedCollectionRetriever namedCollectionRetriever = retrieversFactory.getNamedCollectionRetriever();
        namedCollectionRetriever.setCollectionName(collectionName);
        namedCollectionRetriever.setCollectionFilters(filters);

        return namedCollectionRetriever;
    }

    public QueryCollectionRetriever newQueryCollectionRetriever(String sqlQuery, List<? extends Value<?>> sqlParameters) {
        QueryCollectionRetriever queryCollectionRetriever = retrieversFactory.getQueryCollectionRetriever();
        queryCollectionRetriever.setSqlQuery(sqlQuery);
        queryCollectionRetriever.setSqlParameters(sqlParameters);

        return queryCollectionRetriever;
    }

    public QueryCollectionRetriever newQueryCollectionRetriever(String sqlQuery) {
        QueryCollectionRetriever queryCollectionRetriever = retrieversFactory.getQueryCollectionRetriever();
        queryCollectionRetriever.setSqlQuery(sqlQuery);

        return queryCollectionRetriever;
    }


}
