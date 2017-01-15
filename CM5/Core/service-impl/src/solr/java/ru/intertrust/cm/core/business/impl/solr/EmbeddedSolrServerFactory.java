package ru.intertrust.cm.core.business.impl.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.beans.factory.annotation.Value;

import ru.intertrust.cm.core.tools.DynamicLoadClassFactory;

public class EmbeddedSolrServerFactory implements DynamicLoadClassFactory<SolrServer> {

    @Value("${search.solr.data}")
    private String solrDataDir;

    @Value("${search.solr.home}")
    private String solrHome;

    @Value("${search.solr.collection:CM5}")
    private String solrCollection;

    @Override
    public SolrServer createInstance() {
        if (solrDataDir != null && !solrDataDir.isEmpty()) {
            System.setProperty("solr.data.dir", solrDataDir);
        }
        if (solrHome != null && !solrHome.isEmpty()) {
            System.setProperty("solr.solr.home", solrHome);
        }

        CoreContainer container = new CoreContainer();
        container.load();
        return new EmbeddedSolrServer(container, solrCollection);
    }

}
