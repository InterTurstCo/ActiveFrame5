package ru.intertrust.cm.core.business.impl.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

import ru.intertrust.cm.core.tools.DynamicLoadClassFactory;

public class EmbeddedSolrServerFactory implements DynamicLoadClassFactory<SolrServer> {

    @Override
    public SolrServer createInstance() {
        CoreContainer container = new CoreContainer();
        container.load();
        return new EmbeddedSolrServer(container, "");
    }

}
