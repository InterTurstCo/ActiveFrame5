package ru.intertrust.cm.core.business.impl.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Vitaliy.Orlov on 03.07.2019.
 */
@Component
public class SolrSearchConfiguration {

    @Value("${search.solr.url}")
    private String solrUrl;

    @Value("${search.solr.enable:true}")
    private boolean isSolrEnable;

    @Value("${search.solr.timeout:180000}")
    private int queryTimeout;


    @Value("${search.solr.data}")
    private String solrDataDir;

    @Value("${search.solr.home}")
    private String solrHome;

    @Value("${search.solr.collection:CM5}")
    private String solrCollection;


    public String getSolrUrl() {
        return solrUrl;
    }

    public void setSolrUrl(String solrUrl) {
        this.solrUrl = solrUrl;
    }

    public boolean isSolrEnable() {
        return isSolrEnable;
    }

    public void setSolrEnable(boolean solrEnable) {
        isSolrEnable = solrEnable;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public String getSolrDataDir() {
        return solrDataDir;
    }

    public void setSolrDataDir(String solrDataDir) {
        this.solrDataDir = solrDataDir;
    }

    public String getSolrHome() {
        return solrHome;
    }

    public void setSolrHome(String solrHome) {
        this.solrHome = solrHome;
    }

    public String getSolrCollection() {
        return solrCollection;
    }

    public void setSolrCollection(String solrCollection) {
        this.solrCollection = solrCollection;
    }
}
