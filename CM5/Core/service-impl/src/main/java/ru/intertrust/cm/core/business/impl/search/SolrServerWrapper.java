package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

@Scope(scopeName = "prototype")
public class SolrServerWrapper {
    public static final String REGULAR = "_regular_";
    private SolrClient solrServer = null;
    private boolean bRegular = true;
    private String key = null;
    
    @Autowired
    private SolrUpdateRequestQueue queue;

    public SolrServerWrapper (String key, SolrClient solrServer, boolean isRegular) {
        this.bRegular = isRegular;
        this.key = key != null ? key.trim() : null;
        this.solrServer = solrServer;
    }

    public String getKey() {
        return isRegular() ? REGULAR : key;
    }

    public SolrClient getSolrServer() {
        return solrServer;
    }

    public boolean isRegular() {
        return bRegular;
    }

    public SolrUpdateRequestQueue getQueue() {
        return queue;
    }

}
