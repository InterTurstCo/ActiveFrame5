package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.common.SolrDocumentList;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.util.SpringApplicationContext;

public abstract class CollectionRetriever {

    protected CollectionRetriever() {
        SpringApplicationContext.getContext().getAutowireCapableBeanFactory().autowireBean(this);
    }

    public abstract IdentifiableObjectCollection queryCollection(SolrDocumentList documents, int maxResults);
}
