package ru.intertrust.cm.core.business.impl.search;

import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

@Scope(scopeName = "prototype")
public class SolrServerWrapper {
    public static final String REGULAR = "_regular_";
    private SolrServer solrServer = null;
    private boolean bRegular = true;
    private String url = null;
    private String key = null;
    @Autowired
    private SolrUpdateRequestQueue queue;

    public SolrServerWrapper (String key, String url, SolrServer solrServer, boolean isRegular) {
        this.bRegular = isRegular;
        this.url = url != null ? url.trim() : null;
        this.key = key != null ? key.trim() : null;
        this.solrServer = solrServer;
    }

    public String getKey() {
        return isRegular() ? REGULAR : key;
    }

    public SolrServer getSolrServer() {
        return solrServer;
    }

    public String getUrl() {
        return url;
    }

    public boolean isRegular() {
        return bRegular;
    }

    public SolrUpdateRequestQueue getQueue() {
        return queue;
    }

}
