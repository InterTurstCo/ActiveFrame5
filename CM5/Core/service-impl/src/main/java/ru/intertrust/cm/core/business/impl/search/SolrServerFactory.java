package ru.intertrust.cm.core.business.impl.search;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import ru.intertrust.cm.core.model.FatalException;

public class SolrServerFactory {

    public static final String SOLR_URL_ENTRY = "search.solr.url";
    public static final String SOLR_HOME_ENTRY = "search.solr.home";

    private static final String ENV_ENTRY_PREFIX = "java:comp/env/";
    private static final String SOLR_SERVER_CLASS = "org.apache.solr.client.solrj.embedded.EmbeddedSolrServer";

    private SolrServerFactory() { }

    public static SolrServer getSolrServer() {
        try {
            Context ctx = new InitialContext();
            String url = (String) ctx.lookup(ENV_ENTRY_PREFIX + SOLR_URL_ENTRY);
            if (url != null && !url.isEmpty()) {
                return new HttpSolrServer(url);
            }
            String home = (String) ctx.lookup(ENV_ENTRY_PREFIX + SOLR_HOME_ENTRY);
            if (home != null && !home.isEmpty()) {
                Class<?> clazz = Class.forName(SOLR_SERVER_CLASS);
                //TODO Initialize correctly
                return (SolrServer) clazz.newInstance();
            }
            //TODO Log warning
            return null;
        } catch (Exception e) {
            throw new FatalException("Error initializing search engine", e);
        }
    }
}
