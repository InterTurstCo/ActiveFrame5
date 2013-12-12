package ru.intertrust.cm.core.business.impl.search;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.tools.DynamicLoadClassFactory;

public class SolrServerFactory {

    public static final String SOLR_URL_ENTRY = "search.solr.url";
    public static final String SOLR_HOME_ENTRY = "search.solr.home";

    private static final String ENV_ENTRY_PREFIX = "java:app/env/";
    private static final String SOLR_FACTORY_CLASS =
            "ru.intertrust.cm.core.business.impl.solr.EmbeddedSolrServerFactory";

    private SolrServerFactory() { }

    @SuppressWarnings("unchecked")
    public static SolrServer getSolrServer() {
        try {
            Context ctx = new InitialContext();
            String url = null;
            try {
                url = (String) ctx.lookup(ENV_ENTRY_PREFIX + SOLR_URL_ENTRY);
                      //"http://localhost:8080/solr";
            } catch (NameNotFoundException e) {
                // Нормально, будем пробовать другие варианты
            }
            if (url != null && !url.isEmpty()) {
                return new HttpSolrServer(url);
            }
            String home = null;
            try {
                home = (String) ctx.lookup(ENV_ENTRY_PREFIX + SOLR_HOME_ENTRY);
            } catch (NameNotFoundException e) {
                // Нормально, домашним каталогом будет являться текущий
            }
            if (home != null && !home.isEmpty()) {
                System.setProperty("solr.solr.home", home);
            } else {
                //System.setProperty("solr.solr.home", System.getProperty("user.dir"));
            }
            DynamicLoadClassFactory<SolrServer> factory =
                    (DynamicLoadClassFactory<SolrServer>) Class.forName(SOLR_FACTORY_CLASS).newInstance();
            return factory.createInstance();
        } catch (Exception e) {
            throw new FatalException("Error initializing search engine", e);
        }
    }
}
